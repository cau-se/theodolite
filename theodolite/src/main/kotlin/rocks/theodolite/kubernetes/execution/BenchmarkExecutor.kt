package rocks.theodolite.kubernetes.execution

import mu.KotlinLogging
import rocks.theodolite.core.util.Results
import rocks.theodolite.kubernetes.benchmark.Benchmark
import rocks.theodolite.kubernetes.benchmark.Slo
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.util.PatcherDefinition
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
        val slos: List<Slo>,
        val repetitions: Int,
        val executionId: Int,
        val loadGenerationDelay: Long,
        val afterTeardownDelay: Long,
        val executionName: String,
        val loadPatcherDefinitions: List<PatcherDefinition>,
        val resourcePatcherDefinitions: List<PatcherDefinition>
) {

    var run: AtomicBoolean = AtomicBoolean(true)

    /**
     * Run a experiment for the given parametrization, evaluate the
     * experiment and save the result.
     *
     * @param load to be tested.
     * @param resource to be tested.
     * @return True, if the number of resources are suitable for the
     *     given load, false otherwise (demand metric), or
     *     True, if there is a load suitable for the
     *     given resource, false otherwise.
     */
    abstract fun runExperiment(load: Int, resource: Int): Boolean

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
