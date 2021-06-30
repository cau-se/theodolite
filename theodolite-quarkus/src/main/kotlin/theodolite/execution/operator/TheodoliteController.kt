package theodolite.execution.operator

import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.execution.TheodoliteExecutor
import theodolite.model.crd.*
import theodolite.util.ConfigurationOverride
import theodolite.util.PatcherDefinition
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

/**
 * The controller implementation for Theodolite.
 *
 * @see BenchmarkExecution
 * @see KubernetesBenchmark
 * @see ConcurrentLinkedDeque
 */

class TheodoliteController(
    val path: String,
    private val executionCRDClient: MixedOperation<ExecutionCRD, BenchmarkExecutionList, Resource<ExecutionCRD>>,
    private val benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
    private val executionStateHandler: ExecutionStateHandler
) {
    lateinit var executor: TheodoliteExecutor
    /**
     *
     * Runs the TheodoliteController forever.
     */
    fun run() {
        sleep(5000) // wait until all states are correctly set
        while (true) {
            reconcile()
            sleep(2000)
        }
    }

    private fun reconcile() {
        do {
            val execution = getNextExecution()
            if (execution != null) {
                val benchmark = getBenchmarks()
                    .firstOrNull { it.name == execution.benchmark }
                if (benchmark != null) {
                    runExecution(execution, benchmark)
                }
            } else {
                logger.info { "Could not find executable execution." }
            }
        } while (execution != null)
    }

    /**
     * Execute a benchmark with a defined KubernetesBenchmark and BenchmarkExecution
     *
     * @see BenchmarkExecution
     */
    private fun runExecution(execution: BenchmarkExecution, benchmark: KubernetesBenchmark) {
        setAdditionalLabels(execution.name,
            "deployed-for-execution",
            benchmark.appResource + benchmark.loadGenResource,
            execution)
        setAdditionalLabels(benchmark.name,
            "deployed-for-benchmark",
            benchmark.appResource + benchmark.loadGenResource,
            execution)
        setAdditionalLabels("theodolite",
            "app.kubernetes.io/created-by",
            benchmark.appResource + benchmark.loadGenResource,
            execution)

        executionStateHandler.setExecutionState(execution.name, States.RUNNING)
        executionStateHandler.startDurationStateTimer(execution.name)

        try {
            executor = TheodoliteExecutor(execution, benchmark)
            executor.run()
            when (executionStateHandler.getExecutionState(execution.name)) {
                States.RESTART -> runExecution(execution, benchmark)
                States.RUNNING -> {
                    executionStateHandler.setExecutionState(execution.name, States.FINISHED)
                    logger.info { "Execution of ${execution.name} is finally stopped." }
                }
            }
        } catch (e: Exception) {
            logger.error { "Failure while executing execution ${execution.name} with benchmark ${benchmark.name}." }
            logger.error { "Problem is: $e" }
            executionStateHandler.setExecutionState(execution.name, States.FAILURE)
        }
        executionStateHandler.stopDurationStateTimer()
    }

    @Synchronized
    fun stop(restart: Boolean = false) {
        if (!::executor.isInitialized) return
        if (restart) {
            executionStateHandler.setExecutionState(this.executor.getExecution().name, States.RESTART)
        } else {
            executionStateHandler.setExecutionState(this.executor.getExecution().name, States.INTERRUPTED)
            logger.warn { "Execution ${executor.getExecution().name} unexpected interrupted" }
        }
        this.executor.executor.run.set(false)
    }

    /**
     * @return all available [BenchmarkCRD]s
     */
    private fun getBenchmarks(): List<KubernetesBenchmark> {
        return this.benchmarkCRDClient
            .list()
            .items
            .map { it.spec.name = it.metadata.name; it }
            .map { it.spec.path = path; it } // TODO check if we can remove the path field from the KubernetesBenchmark
            .map { it.spec }
    }

    /**
     * Get the [BenchmarkExecution] for the next run. Which [BenchmarkExecution]
     * is selected for the next execution depends on three points:
     *
     * 1. Only executions are considered for which a matching benchmark is available on the cluster
     * 2. The Status of the execution must be [States.PENDING] or [States.RESTART]
     * 3. Of the remaining [BenchmarkCRD], those with status [States.RESTART] are preferred,
     * then, if there is more than one, the oldest execution is chosen.
     *
     * @return the next execution or null
     */
    private fun getNextExecution(): BenchmarkExecution? {
        val availableBenchmarkNames = getBenchmarks()
            .map { it.name }

        return executionCRDClient
            .list()
            .items
            .asSequence()
            .map { it.spec.name = it.metadata.name; it }
            .filter {
                it.status.executionState == States.PENDING.value ||
                        it.status.executionState == States.RESTART.value
            }
            .filter { availableBenchmarkNames.contains(it.spec.benchmark) }
            .sortedWith(stateComparator().thenBy { it.metadata.creationTimestamp })
            .map { it.spec }
            .firstOrNull()
    }

    /**
     * Simple comparator which can be used to order a list of [ExecutionCRD] such that executions with
     * status [States.RESTART] are before all other executions.
     */
    private fun stateComparator() = Comparator<ExecutionCRD> { a, b ->
        when {
            (a == null && b == null) -> 0
            (a.status.executionState == States.RESTART.value) -> -1
            else -> 1
        }
    }

    fun isExecutionRunning(executionName: String): Boolean {
        if (!::executor.isInitialized) return false
        return this.executor.getExecution().name == executionName
    }

    private fun setAdditionalLabels(
        labelValue: String,
        labelName: String,
        resources: List<String>,
        execution: BenchmarkExecution
    ) {
        val additionalConfigOverrides = mutableListOf<ConfigurationOverride>()
        resources.forEach {
            run {
                val configurationOverride = ConfigurationOverride()
                configurationOverride.patcher = PatcherDefinition()
                configurationOverride.patcher.type = "LabelPatcher"
                configurationOverride.patcher.properties = mutableMapOf("variableName" to labelName)
                configurationOverride.patcher.resource = it
                configurationOverride.value = labelValue
                additionalConfigOverrides.add(configurationOverride)
            }
        }
        execution.configOverrides.addAll(additionalConfigOverrides)
    }
}