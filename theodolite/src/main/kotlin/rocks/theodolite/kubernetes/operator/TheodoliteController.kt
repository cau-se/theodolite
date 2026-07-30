package rocks.theodolite.kubernetes.operator

import mu.KotlinLogging
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}
const val DEPLOYED_FOR_EXECUTION_LABEL_NAME = "deployed-for-execution"
const val DEPLOYED_FOR_BENCHMARK_LABEL_NAME = "deployed-for-benchmark"
const val CREATED_BY_LABEL_NAME = "app.kubernetes.io/created-by"
const val CREATED_BY_LABEL_VALUE = "theodolite"

/**
 * The controller implementation for Theodolite.
 *
 * Drives the 2-second fabric8 reconcile loop and delegates all selection/run/stop
 * logic to [RunnerCoordinator].
 */
class TheodoliteController(
        private val coordinator: RunnerCoordinator,
        private val benchmarkStateChecker: BenchmarkStateChecker,
) {

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
            val ran = coordinator.selectAndRunExecution()
            if (!ran) {
                logger.info { "Could not find executable execution." }
            }
        } while (ran)
    }

    @Synchronized
    fun stop(restart: Boolean = false) {
        coordinator.stop(restart)
    }

    fun isExecutionRunning(executionName: String): Boolean {
        return coordinator.isRunning(executionName)
    }
}