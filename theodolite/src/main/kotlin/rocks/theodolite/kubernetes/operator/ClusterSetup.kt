package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import rocks.theodolite.kubernetes.k8s.K8sContextFactory
import rocks.theodolite.kubernetes.k8s.ResourceByLabelHandler
import rocks.theodolite.kubernetes.model.crd.*
import rocks.theodolite.kubernetes.Shutdown


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
                    Shutdown(execution.spec, benchmark.spec).run()
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
            labelValue = "rocks/theodolitedolite"
        )
        resourceRemover.removeDeployments(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "rocks/theodolitedolite"
        )
        resourceRemover.removeStatefulSets(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "rocks/theodolitedolite"
        )
        resourceRemover.removeConfigMaps(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "rocks/theodolitedolite"
        )
        resourceRemover.removeCR(
            labelName = "app.kubernetes.io/created-by",
            labelValue = "rocks/theodolitedolite",
            context = serviceMonitorContext
        )
    }
}