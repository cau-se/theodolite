package theodolite.execution.operator

import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.execution.TheodoliteExecutor
import theodolite.model.crd.*
import theodolite.patcher.ConfigOverrideModifier
import theodolite.util.ExecutionComparator
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
        val modifier = ConfigOverrideModifier(
            execution = execution,
            resources = benchmark.appResource + benchmark.loadGenResource)
        modifier.setAdditionalLabels(
            labelValue = execution.name,
            labelName = "deployed-for-execution"
        )
        modifier.setAdditionalLabels(
            labelValue = benchmark.name,
            labelName = "deployed-for-benchmark"
        )
        modifier.setAdditionalLabels(
            labelValue = "theodolite",
            labelName = "app.kubernetes.io/created-by"
        )

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
            .map {
                it.spec.name = it.metadata.name;
                it.spec
            }
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
        val comparator = ExecutionComparator().compareByState(States.RESTART)
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
            .sortedWith(comparator.thenBy { it.metadata.creationTimestamp })
            .map { it.spec }
            .firstOrNull()
    }

    fun isExecutionRunning(executionName: String): Boolean {
        if (!::executor.isInitialized) return false
        return this.executor.getExecution().name == executionName
    }
}