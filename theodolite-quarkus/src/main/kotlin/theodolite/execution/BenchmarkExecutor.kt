package theodolite.execution

import mu.KotlinLogging
import theodolite.util.Benchmark
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
abstract class BenchmarkExecutor(val benchmark: Benchmark, val results: Results, val executionDuration: Duration) {
    /**
     * Run a experiment for the given parametrization, evaluate the experiment and save the result.
     *
     * @param load load to be tested.
     * @param res resources to be tested.
     * @return True, if the number of resources are suitable for the given load, false otherwise.
     */
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean;

    /**
     * Wait while the benchmark is running and log the number of minutes executed every 1 minute.
     *
     */
    fun waitAndLog() {
        for (i in 1.rangeTo(executionDuration.toMinutes())) {
            Thread.sleep(Duration.ofMinutes(1).toMillis())
            logger.info { "Executed: $i minutes" }
        }
    }
}