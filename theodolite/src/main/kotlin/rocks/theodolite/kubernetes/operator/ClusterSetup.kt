package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import rocks.theodolite.kubernetes.K8sContextFactory
import rocks.theodolite.kubernetes.ResourceByLabelHandler
import rocks.theodolite.kubernetes.model.crd.*
import rocks.theodolite.kubernetes.Shutdown

private val logger = KotlinLogging.logger {}

class ClusterSetup(
        private val executionCRDClient: MixedOperation<ExecutionCRD, BenchmarkExecutionList, Resource<ExecutionCRD>>,
        private val benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
        private val client: NamespacedKubernetesClient

) {
    private val serviceMonitorContext = K8sContextFactory().create(
        api = "v1",
        scope = "Namespaced",
        group = "monitoring.coreos.com",
        plural = "servicemonitors"
    )

    fun clearClusterState() {
        stopRunningExecution()
        clearByLabel()
    }

    /**
     * This function searches for executions in the cluster that have the status running and tries to stop the execution.
     * For this the corresponding benchmark is searched and terminated.
     *
     * Throws [IllegalStateException] if no suitable benchmark can be found.
     *
     */
    private fun stopRunningExecution() {
        executionCRDClient
            .list()
            .items
            .asSequence()
            .filter { it.status.executionState == ExecutionState.RUNNING }
            .forEach { execution ->
                val benchmark = benchmarkCRDClient
                    .inNamespace(client.namespace)
                    .list()
                    .items
                    .firstOrNull { it.metadata.name == execution.spec.benchmark }

                if (benchmark != null) {
                    execution.spec.name = execution.metadata.name
                    benchmark.spec.name = benchmark.metadata.name
                    Shutdown(execution.spec, benchmark.spec, client).run()
                } else {
                    throw IllegalStateException("Execution with state ${ExecutionState.RUNNING.value} was found, but no corresponding benchmark. " +
                            "Could not initialize cluster.")
                }
            }
    }

    private fun clearByLabel() {
        val resourceRemover = ResourceByLabelHandler(client = client)
        resourceRemover.removeServices(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "theodolite"
        )
        resourceRemover.removeDeployments(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "theodolite"
        )
        resourceRemover.removeStatefulSets(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "theodolite"
        )
        resourceRemover.removeConfigMaps(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "theodolite"
        )
        try {
            resourceRemover.removeGenericResources(
                labelName = "app.kubernetes.io/created-by",
                labelValue = "theodolite",
                context = serviceMonitorContext
            )
        } catch (e: KubernetesClientException) {
            logger.warn { "Service monitors could not be cleaned up. It may be that service monitors are not registered by the Kubernetes API."}
            logger.debug { "Error is: ${e.message}" }
        }
    }
}