package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.TheodoliteExecutor
import rocks.theodolite.kubernetes.model.crd.*
import rocks.theodolite.kubernetes.patcher.ConfigOverrideModifier
import rocks.theodolite.kubernetes.model.crd.ExecutionStateComparator
import rocks.theodolite.kubernetes.loadKubernetesResources
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}
const val DEPLOYED_FOR_EXECUTION_LABEL_NAME = "deployed-for-execution"
const val DEPLOYED_FOR_BENCHMARK_LABEL_NAME = "deployed-for-benchmark"
const val CREATED_BY_LABEL_NAME = "app.kubernetes.io/created-by"
const val CREATED_BY_LABEL_VALUE = "rocks/theodolite"

/**
 * The controller implementation for Theodolite.
 *
 * @see BenchmarkExecution
 * @see KubernetesBenchmark
 */

class TheodoliteController(
        private val client: NamespacedKubernetesClient,
        private val executionCRDClient: MixedOperation<ExecutionCRD, BenchmarkExecutionList, Resource<ExecutionCRD>>,
        private val benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
        private val executionStateHandler: ExecutionStateHandler,
        private val benchmarkStateChecker: BenchmarkStateChecker,

) {
    lateinit var executor: TheodoliteExecutor

    /**
     * Runs the TheodoliteController forever.
     */
    fun run() {
        sleep(5000) // wait until all states are correctly set
        benchmarkStateChecker.start(true)
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
                    .map { it.spec }
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
        try {
            val modifier = ConfigOverrideModifier(
                    execution = execution,
                    resources = loadKubernetesResources(benchmark.sut.resources, this.client).map { it.first }
                            + loadKubernetesResources(benchmark.loadGenerator.resources, this.client).map { it.first }
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

            executionStateHandler.setExecutionState(execution.name, ExecutionState.RUNNING)
            executionStateHandler.startDurationStateTimer(execution.name)

            executor = TheodoliteExecutor(execution, benchmark, this.client)
            executor.setupAndRunExecution()
            when (executionStateHandler.getExecutionState(execution.name)) {
                ExecutionState.RESTART -> runExecution(execution, benchmark)
                ExecutionState.RUNNING -> {
                    executionStateHandler.setExecutionState(execution.name, ExecutionState.FINISHED)
                    logger.info { "Execution of ${execution.name} is finally stopped." }
                    }
                else -> {
                    executionStateHandler.setExecutionState(execution.name, ExecutionState.FAILURE)
                    logger.warn { "Unexpected execution state, set state to ${ExecutionState.FAILURE.value}." }
                }
            }
        } catch (e: Exception) {
            EventCreator().createEvent(
                executionName = execution.name,
                type = "WARNING",
                reason = "Execution failed",
                message = "An error occurs while executing:  ${e.message}")
            logger.error(e) { "Failure while executing execution ${execution.name} with benchmark ${benchmark.name}." }
            executionStateHandler.setExecutionState(execution.name, ExecutionState.FAILURE)
        }
        executionStateHandler.stopDurationStateTimer(execution.name)
    }

    @Synchronized
    fun stop(restart: Boolean = false) {
        if (!::executor.isInitialized) return
        if (restart) {
            executionStateHandler.setExecutionState(this.executor.getExecution().name, ExecutionState.RESTART)
        }
        this.executor.experimentRunner.run.set(false)
    }

    /**
     * @return all available [BenchmarkCRD]s
     */
    private fun getBenchmarks(): List<BenchmarkCRD> {
        return this.benchmarkCRDClient
            .list()
            .items
            .map {
                it.apply { it.spec.name = it.metadata.name }
            }
    }

    /**
     * Get the [BenchmarkExecution] for the next run. Which [BenchmarkExecution]
     * is selected for the next execution depends on three points:
     *
     * 1. Only executions are considered for which a matching benchmark is available on the cluster
     * 2. The Status of the execution must be [ExecutionState.PENDING] or [ExecutionState.RESTART]
     * 3. Of the remaining [BenchmarkCRD], those with status [ExecutionState.RESTART] are preferred,
     * then, if there is more than one, the oldest execution is chosen.
     *
     * @return the next execution or null
     */
    private fun getNextExecution(): BenchmarkExecution? {
        val comparator = ExecutionStateComparator(ExecutionState.RESTART)
        val availableBenchmarkNames = getBenchmarks()
            .filter { it.status.resourceSetsState == BenchmarkState.READY }
            .map { it.spec }
            .map { it.name }

        return executionCRDClient
            .list()
            .items
            .asSequence()
            .map { it.spec.name = it.metadata.name; it }
            .filter {
                it.status.executionState == ExecutionState.PENDING || it.status.executionState == ExecutionState.RESTART
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