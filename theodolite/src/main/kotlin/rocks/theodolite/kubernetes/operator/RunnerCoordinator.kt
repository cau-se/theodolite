package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import rocks.theodolite.kubernetes.loadKubernetesResources
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.patcher.ConfigOverrideModifier
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

const val DEPLOYED_FOR_EXECUTION_LABEL_NAME = "deployed-for-execution"
const val DEPLOYED_FOR_BENCHMARK_LABEL_NAME = "deployed-for-benchmark"
const val CREATED_BY_LABEL_NAME = "app.kubernetes.io/created-by"
const val CREATED_BY_LABEL_VALUE = "theodolite"

/**
 * CDI singleton that owns [TheodoliteRunner], the execution selection/ordering logic, and the
 * in-memory runtime state of the currently active benchmark run.
 *
 * The coordinator never writes Execution status itself.  Instead it holds the runtime state
 * (which execution is running and any pending terminal result) and asks [ExecutionReconciler]
 * to persist it by triggering a reconcile through the registered [ExecutionEventSource].  This
 * keeps [ExecutionReconciler] the single writer of Execution status.
 *
 * **Capacity 1:** at most one execution runs at a time.  The embedded [TheodoliteRunner] uses a
 * single-thread executor, and [triggerSelection] is synchronized and starts a new run only when
 * none is active, so global ordering (interrupted-first, then oldest) is strictly enforced.
 */
// @Unremovable: injected into ExecutionReconciler and looked up via Arc.container() in
// TheodoliteOperator; keep it even though Quarkus's build-time optimizer might drop it.
@Unremovable
@ApplicationScoped
class RunnerCoordinator {

    /**
     * Kubernetes client used to list Executions/Benchmarks and to apply deployment labels.
     * Set by CDI in production; set directly in tests.
     */
    @Inject
    lateinit var client: KubernetesClient

    private val runner: TheodoliteRunner

    /** CDI no-arg constructor. */
    constructor() : this(TheodoliteRunner())

    /** Test constructor — pass a custom runner for unit tests. */
    internal constructor(runner: TheodoliteRunner) {
        this.runner = runner
    }

    /** Event source used to trigger reconciles; registered by [ExecutionReconciler]. */
    @Volatile
    private var trigger: ExecutionEventSource? = null

    /** The currently active run, or `null` when the runner is idle. */
    private data class ActiveRun(val name: String, val generation: Long?, val startTime: Instant)

    /** A terminal result awaiting persistence by [ExecutionReconciler]. */
    internal data class Completion(
        val state: ExecutionState,
        val startTime: Instant,
        val completionTime: Instant
    )

    @Volatile
    private var active: ActiveRun? = null

    private val completions = ConcurrentHashMap<String, Completion>()
    private val respecStops = ConcurrentHashMap.newKeySet<String>()
    private val deletionStops = ConcurrentHashMap.newKeySet<String>()

    /** Registers the event source used to trigger execution reconciles. */
    fun registerTrigger(eventSource: ExecutionEventSource) {
        this.trigger = eventSource
    }

    // --- state read by ExecutionReconciler ---------------------------------------------------

    /** Name of the currently active execution, or `null` if none is running. */
    fun activeExecutionName(): String? = active?.name

    /** Generation of the currently active execution, or `null` if none is running. */
    fun activeGeneration(): Long? = active?.generation

    /** Start time of the currently active execution, or `null` if none is running. */
    fun activeStartTime(): Instant? = active?.startTime

    /** Pending terminal result for [name], or `null` if none is awaiting persistence. */
    internal fun completionFor(name: String): Completion? = completions[name]

    /** Clears the pending terminal result for [name] once it has been persisted. */
    fun clearCompletion(name: String) {
        completions.remove(name)
    }

    /** Returns `true` if the execution with the given name is currently running. */
    fun isRunning(executionName: String): Boolean =
        active?.name == executionName && runner.isRunning(executionName)

    // --- run control -------------------------------------------------------------------------

    /**
     * Selects the next eligible execution and starts it, unless a run is already active.
     * Fast and non-blocking: the benchmark itself runs on the [TheodoliteRunner] thread.
     */
    @Synchronized
    fun triggerSelection() {
        if (active != null) {
            return
        }
        val (execution, benchmark) = selectNext() ?: return
        val name = execution.metadata.name
        val startTime = Instant.now()
        active = ActiveRun(name, execution.metadata.generation, startTime)
        logger.info { "Starting execution '$name' with benchmark '${benchmark.name}'." }
        propagate(name)

        val spec = execution.spec
        runner.start(
            executionName = name,
            execution = spec,
            benchmark = benchmark,
            client = namespacedClient(),
            beforeRun = { applyLabels(name, spec, benchmark) },
            isCancelled = { respecStops.contains(name) || deletionStops.contains(name) }
        ) { error -> onRunComplete(name, startTime, error) }
    }

    /**
     * Invoked on the runner thread when a run finishes.  Records the terminal result (unless the
     * run was stopped for a spec change or deletion), triggers the reconciler to persist it, and
     * selects the next execution.
     */
    @Synchronized
    private fun onRunComplete(name: String, startTime: Instant, error: Throwable?) {
        active = null
        when {
            deletionStops.remove(name) -> {
                logger.info { "Execution '$name' was stopped for deletion." }
            }
            respecStops.remove(name) -> {
                logger.info { "Execution '$name' was stopped for a spec change and will be re-run." }
                propagate(name)
            }
            else -> {
                val state = if (error != null) ExecutionState.FAILURE else ExecutionState.FINISHED
                completions[name] = Completion(state, startTime, Instant.now())
                if (error != null) {
                    logger.error(error) { "Failure while executing execution '$name'." }
                    EventCreator().createEvent(
                        executionName = name,
                        type = "WARNING",
                        reason = "Execution failed",
                        message = "An error occurs while executing:  ${error.message}"
                    )
                } else {
                    logger.info { "Execution '$name' is finally stopped." }
                }
                propagate(name)
            }
        }
        triggerSelection()
    }

    /**
     * Stops the active execution because its spec changed; it stays eligible and will be re-run.
     * Also cancels a run that is still queued (executor not yet created) so the obsolete spec is
     * never deployed.
     */
    @Synchronized
    fun stopForRespec(name: String) {
        if (active?.name == name) {
            respecStops.add(name)
            runner.stop()
        }
    }

    /**
     * Stops the active execution because it is being deleted; no terminal status is written.
     * Also cancels a run that is still queued (executor not yet created) so nothing is deployed
     * after the CR has gone away.
     */
    @Synchronized
    fun stopForDeletion(name: String) {
        if (active?.name == name) {
            deletionStops.add(name)
            runner.stop()
        }
    }

    // --- selection ---------------------------------------------------------------------------

    /**
     * Selects the next eligible execution together with its benchmark spec.
     *
     * An execution is eligible when its benchmark is [BenchmarkState.READY] and its state is
     * [ExecutionState.PENDING] or [ExecutionState.RUNNING].  Because this runs only while the
     * runner is idle, a `RUNNING` state means the execution was interrupted mid-run (e.g. by an
     * operator restart or a spec change) and should resume.  Executions with a pending terminal
     * result are excluded.
     *
     * Interrupted-mid-run executions are preferred, then the oldest by `creationTimestamp`.
     */
    internal fun selectNext(): Pair<ExecutionCRD, KubernetesBenchmark>? {
        val readyBenchmarks = getBenchmarks()
            .filter { it.status.resourceSetsState == BenchmarkState.READY }
        val readyNames = readyBenchmarks.map { it.spec.name }.toSet()

        val interruptedFirst = Comparator<ExecutionCRD> { a, b ->
            val aRank = if (a.status.executionState == ExecutionState.RUNNING) 0 else 1
            val bRank = if (b.status.executionState == ExecutionState.RUNNING) 0 else 1
            aRank - bRank
        }

        val candidate = listExecutions()
            .asSequence()
            .filter {
                it.status.executionState == ExecutionState.PENDING ||
                    it.status.executionState == ExecutionState.RUNNING
            }
            .filter { !completions.containsKey(it.metadata.name) }
            .filter { readyNames.contains(it.spec.benchmark) }
            .sortedWith(interruptedFirst.thenBy { it.metadata.creationTimestamp })
            .firstOrNull() ?: return null

        val benchmark = readyBenchmarks
            .firstOrNull { it.spec.name == candidate.spec.benchmark }
            ?.spec ?: return null
        return candidate to benchmark
    }

    /**
     * Applies the Theodolite ownership labels to the SUT and load-generator resources so that
     * they can be cleaned up per execution/benchmark.
     */
    private fun applyLabels(executionName: String, execution: BenchmarkExecution, benchmark: KubernetesBenchmark) {
        val c = namespacedClient()
        val modifier = ConfigOverrideModifier(
            execution = execution,
            resources = loadKubernetesResources(benchmark.sut.resources, c).map { it.first } +
                loadKubernetesResources(benchmark.loadGenerator.resources, c).map { it.first }
        )
        modifier.setAdditionalLabels(
            labelValue = executionName,
            labelName = DEPLOYED_FOR_EXECUTION_LABEL_NAME
        )
        modifier.setAdditionalLabels(
            labelValue = benchmark.name,
            labelName = DEPLOYED_FOR_BENCHMARK_LABEL_NAME
        )
        modifier.setAdditionalLabels(
            labelValue = CREATED_BY_LABEL_VALUE,
            labelName = CREATED_BY_LABEL_NAME
        )
    }

    /** Returns all available [BenchmarkCRD]s with their spec name populated. */
    internal fun getBenchmarks(): List<BenchmarkCRD> {
        return namespacedClient()
            .resources(BenchmarkCRD::class.java)
            .list()
            .items
            .map { it.apply { it.spec.name = it.metadata.name } }
    }

    private fun listExecutions(): List<ExecutionCRD> {
        return namespacedClient()
            .resources(ExecutionCRD::class.java)
            .list()
            .items
    }

    private fun propagate(name: String) {
        trigger?.propagateEvent(ResourceID(name, namespacedClient().namespace))
    }

    // The client CDI injects in production is a normal-scoped client proxy. It implements the bean
    // type of the producer method, KubernetesClient, but not NamespacedKubernetesClient, so casting
    // it fails even though the client it delegates to does implement the latter. Adapting asks that
    // underlying client instead.
    private fun namespacedClient(): NamespacedKubernetesClient =
        client.adapt(NamespacedKubernetesClient::class.java)
}
