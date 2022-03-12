package rocks.theodolite.kubernetes

import mu.KotlinLogging
import rocks.theodolite.kubernetes.execution.KubernetesExecutionRunner
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark

private val logger = KotlinLogging.logger {}

/**
 * This Shutdown Hook can be used to delete all Kubernetes resources which are related to the given execution and benchmark.
 *
 * @property benchmarkExecution
 * @property benchmark
 */
class Shutdown(private val benchmarkExecution: BenchmarkExecution, private val benchmark: KubernetesBenchmark) {

    /**
     * Run
     * Delete all Kubernetes resources which are related to the execution and the benchmark.
     */
    fun run() {
        // Build Configuration to teardown
        val kubernetesExecutionRunner = KubernetesExecutionRunner(benchmark)
        try {
            logger.info { "Received shutdown signal -> Shutting down" }
            val deployment =
                    kubernetesExecutionRunner.buildDeployment(
                    load = 0,
                    loadPatcherDefinitions = emptyList(),
                    resource = 0,
                    resourcePatcherDefinitions = emptyList(),
                    configurationOverrides = benchmarkExecution.configOverrides,
                    loadGenerationDelay = 0L,
                    afterTeardownDelay = 5L
                )
            deployment.teardown()
            logger.info { "Finished teardown of all benchmark resources." }
        } catch (e: Exception) {
            logger.warn {
                "Could not delete all specified resources from Kubernetes. " +
                        "This could be the case, if not all resources are deployed and running."
            }

        }
    }
}
