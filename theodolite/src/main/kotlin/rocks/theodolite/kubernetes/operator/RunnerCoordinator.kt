package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import jakarta.enterprise.context.ApplicationScoped
import mu.KotlinLogging
import rocks.theodolite.kubernetes.loadKubernetesResources
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkExecutionList
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.model.crd.ExecutionStateComparator
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList
import rocks.theodolite.kubernetes.patcher.ConfigOverrideModifier

private val logger = KotlinLogging.logger {}

/**
 * CDI singleton that owns [TheodoliteRunner], the execution selection/ordering logic,
 * and duration timing for each run.
 *
 * Still fabric8-triggered in Stage 2: [TheodoliteController] calls [selectAndRunExecution]
 * from its 2-second reconcile loop.  In later stages, [ExecutionReconciler] will inject and
 * call this coordinator, and the fabric8 machinery will be retired.
 *
 * **Single-thread guarantee:** the embedded [TheodoliteRunner] uses a single-thread executor,
 * so at most one execution is active at any given time and ordering is strictly enforced.
 *
 * Call [initialize] once (from [TheodoliteOperator]) before any other method.
 */
@ApplicationScoped
class RunnerCoordinator {

    private val runner: TheodoliteRunner

    /** CDI no-arg constructor. */
    constructor() : this(TheodoliteRunner())

    /** Test constructor — pass a custom runner for unit tests. */
    internal constructor(runner: TheodoliteRunner) {
        this.runner = runner
    }

    @Volatile private var client: NamespacedKubernetesClient? = null
    @Volatile private var executionCRDClient: MixedOperation<ExecutionCRD, BenchmarkExecutionList, Resource<ExecutionCRD>>? = null
    @Volatile private var benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>? = null
    @Volatile private var executionStateHandler: ExecutionStateHandler? = null

    /**
     * Initializes this coordinator with the Kubernetes client and CRD clients.
     * Must be called once before any other method.
     */
    fun initialize(
        client: NamespacedKubernetesClient,
        executionCRDClient: MixedOperation<ExecutionCRD, BenchmarkExecutionList, Resource<ExecutionCRD>>,
        benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
        executionStateHandler: ExecutionStateHandler
    ) {
        this.client = client
        this.executionCRDClient = executionCRDClient
        this.benchmarkCRDClient = benchmarkCRDClient
        this.executionStateHandler = executionStateHandler
    }

    /**
     * Selects the next eligible execution and runs it synchronously.
     *
     * @return `true` if an execution was found and run; `false` if no eligible execution was found.
     */
    fun selectAndRunExecution(): Boolean {
        val execution = getNextExecution() ?: return false
        val benchmark = getBenchmarks()
            .map { it.spec }
            .firstOrNull { it.name == execution.benchmark } ?: return false
        runExecution(execution, benchmark)
        return true
    }

    /**
     * Runs the given [execution] synchronously, including label patching, state transitions,
     * and duration timing.  Handles the `RESTART` state by re-running recursively.
     */
    private fun runExecution(execution: BenchmarkExecution, benchmark: KubernetesBenchmark) {
        val c = requireClient()
        val stateHandler = requireStateHandler()
        try {
            val modifier = ConfigOverrideModifier(
                execution = execution,
                resources = loadKubernetesResources(benchmark.sut.resources, c).map { it.first }
                        + loadKubernetesResources(benchmark.loadGenerator.resources, c).map { it.first }
            )
            modifier.setAdditionalLabels(
                labelValue = execution.name,
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

            stateHandler.setExecutionState(execution.name, ExecutionState.RUNNING)
            stateHandler.startDurationStateTimer(execution.name)

            runner.run(execution, benchmark, c)

            when (stateHandler.getExecutionState(execution.name)) {
                ExecutionState.RESTART -> runExecution(execution, benchmark)
                ExecutionState.RUNNING -> {
                    stateHandler.setExecutionState(execution.name, ExecutionState.FINISHED)
                    logger.info { "Execution of ${execution.name} is finally stopped." }
                }
                else -> {
                    stateHandler.setExecutionState(execution.name, ExecutionState.FAILURE)
                    logger.warn { "Unexpected execution state, set state to ${ExecutionState.FAILURE.value}." }
                }
            }
        } catch (e: Exception) {
            EventCreator().createEvent(
                executionName = execution.name,
                type = "WARNING",
                reason = "Execution failed",
                message = "An error occurs while executing:  ${e.message}"
            )
            logger.error(e) { "Failure while executing execution ${execution.name} with benchmark ${benchmark.name}." }
            stateHandler.setExecutionState(execution.name, ExecutionState.FAILURE)
        }
        stateHandler.stopDurationStateTimer(execution.name)
    }

    /**
     * Returns all available [BenchmarkCRD]s.
     */
    internal fun getBenchmarks(): List<BenchmarkCRD> {
        return requireBenchmarkCRDClient()
            .list()
            .items
            .map { it.apply { it.spec.name = it.metadata.name } }
    }

    /**
     * Selects the next eligible [BenchmarkExecution].
     *
     * An execution is eligible if:
     * 1. Its state is [ExecutionState.PENDING] or [ExecutionState.RESTART].
     * 2. Its referenced benchmark is available and in state [BenchmarkState.READY].
     *
     * Among eligible executions, those with state [ExecutionState.RESTART] are preferred,
     * then the oldest by `creationTimestamp`.
     */
    internal fun getNextExecution(): BenchmarkExecution? {
        val comparator = ExecutionStateComparator(ExecutionState.RESTART)
        val availableBenchmarkNames = getBenchmarks()
            .filter { it.status.resourceSetsState == BenchmarkState.READY }
            .map { it.spec.name }

        return requireExecutionCRDClient()
            .list()
            .items
            .asSequence()
            .map { it.spec.name = it.metadata.name; it }
            .filter {
                it.status.executionState == ExecutionState.PENDING ||
                        it.status.executionState == ExecutionState.RESTART
            }
            .filter { availableBenchmarkNames.contains(it.spec.benchmark) }
            .sortedWith(comparator.thenBy { it.metadata.creationTimestamp })
            .map { it.spec }
            .firstOrNull()
    }

    /**
     * Signals the currently running execution to stop.
     * If [restart] is `true`, the execution state is set to [ExecutionState.RESTART] first.
     */
    @Synchronized
    fun stop(restart: Boolean = false) {
        if (restart) {
            runner.getExecution()?.let { execution ->
                requireStateHandler().setExecutionState(execution.name, ExecutionState.RESTART)
            }
        }
        runner.stop()
    }

    /** Returns `true` if the execution with the given name is currently running. */
    fun isRunning(executionName: String): Boolean = runner.isRunning(executionName)

    private fun requireClient(): NamespacedKubernetesClient =
        client ?: error("RunnerCoordinator has not been initialized — call initialize() first")

    private fun requireStateHandler(): ExecutionStateHandler =
        executionStateHandler ?: error("RunnerCoordinator has not been initialized — call initialize() first")

    private fun requireExecutionCRDClient() =
        executionCRDClient ?: error("RunnerCoordinator has not been initialized — call initialize() first")

    private fun requireBenchmarkCRDClient() =
        benchmarkCRDClient ?: error("RunnerCoordinator has not been initialized — call initialize() first")
}
