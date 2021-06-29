package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import java.lang.Exception

private val logger = KotlinLogging.logger {}

/**
 * This Shutdown Hook can be used to delete all Kubernetes resources which are related to the given execution and benchmark.
 *
 * @property benchmarkExecution
 * @property benchmark
 */
class Shutdown(private val benchmarkExecution: BenchmarkExecution, private val benchmark: KubernetesBenchmark) :
    Thread() {

    /**
     * Run
     * Delete all Kubernetes resources which are related to the execution and the benchmark.
     */
    override fun run() {
        // Build Configuration to teardown
        try {
        logger.info { "Received shutdown signal -> Shutting down" }
        val deployment =
            benchmark.buildDeployment(
                load = LoadDimension(0, emptyList()),
                res = Resource(0, emptyList()),
                configurationOverrides = benchmarkExecution.configOverrides,
                loadGenerationDelay = 0L,
                afterTeardownDelay = 5L
            )
            deployment.teardown()
        } catch (e: Exception) {
            logger.warn { "Could not delete all specified resources from Kubernetes. " +
                    "This could be the case, if not all resources are deployed and running." }

        }
        logger.info { "Teardown everything deployed" }
        logger.info { "Teardown completed" }
    }
}
