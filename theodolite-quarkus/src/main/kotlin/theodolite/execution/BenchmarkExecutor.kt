package theodolite.execution

import io.smallrye.mutiny.helpers.Subscriptions.cancel
import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

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
    configurationOverrides: List<ConfigurationOverride?>,
    val slo: BenchmarkExecution.Slo
) {

    /**
     * Run a experiment for the given parametrization, evaluate the experiment and save the result.
     *
     * @param load load to be tested.
     * @param res resources to be tested.
     * @return True, if the number of resources are suitable for the given load, false otherwise.
     */
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean

    fun stop() {
        throw InterruptedException()
    }

    /**
     * Wait while the benchmark is running and log the number of minutes executed every 1 minute.
     *
     */
    fun waitAndLog() {
        logger.info { "Execution of a new benchmark started." }
        for (i in 1.rangeTo(executionDuration.toSeconds())) {

            Thread.sleep(Duration.ofSeconds(1).toMillis())
            if ((i % 60) == 0L) {
                logger.info { "Executed: ${i / 60} minutes" }
            }
        }
    }
}
