package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

class Shutdown(private val benchmarkExecution: BenchmarkExecution, private val benchmark: KubernetesBenchmark) {

    fun run() {
        // Build Configuration to teardown
        logger.info { "Received shutdown signal -> Shutting down" }
        val deployment =
            benchmark.buildDeployment(
                load = LoadDimension(0, emptyList()),
                res = Resource(0, emptyList()),
                configurationOverrides = benchmarkExecution.configOverrides
            )
        logger.info { "Teardown everything deployed" }
        deployment.teardown()
        logger.info { "Teardown completed" }
    }
}