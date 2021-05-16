package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

/**
 * The Benchmark Executor runs a single experiment.
 *
 * @property benchmark
 * @property results
 * @property executionDuration
 * @constructor Create empty Benchmark executor
 */
abstract class BenchmarkExecutor(
    val benchmark: Benchmark,
    val results: Results,
    val executionDuration: Duration,
    val configurationOverrides: List<ConfigurationOverride?>,
    val slo: BenchmarkExecution.Slo,
    val repetitions: Int,
    val executionId: Int,
    val loadGenerationDelay: Long,
    val afterTeardownDelay: Long
) {

    var run: AtomicBoolean = AtomicBoolean(true)

    /**
     * Run a experiment for the given parametrization, evaluate the
     * experiment and save the result.
     *
     * @param load load to be tested.
     * @param res resources to be tested.
     * @return True, if the number of resources are suitable for the
     *     given load, false otherwise.
     */
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean

    /**
     * Wait while the benchmark is running and log the number of minutes executed every 1 minute.
     *
     */
    fun waitAndLog() {
        logger.info { "Execution of a new experiment started." }

        var secondsRunning = 0L

        while (run.get() && secondsRunning < executionDuration.toSeconds()) {
            secondsRunning++
            Thread.sleep(Duration.ofSeconds(1).toMillis())

            if ((secondsRunning % 60) == 0L) {
                logger.info { "Executed: ${secondsRunning / 60} minutes." }
            }
        }

        logger.debug { "Executor shutdown gracefully." }

    }
}
