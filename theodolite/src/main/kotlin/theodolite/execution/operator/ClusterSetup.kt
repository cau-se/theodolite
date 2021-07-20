package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import mu.KotlinLogging
import org.json.JSONObject
import theodolite.execution.Shutdown
import theodolite.k8s.K8sContextFactory
import theodolite.model.crd.*

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

    fun clearClusterState(){
        stopRunningExecution()
        clearByLabel()
    }

    private fun stopRunningExecution() {
        executionCRDClient
            .inNamespace(client.namespace)
            .list()
            .items
            .asSequence()
            .filter {   it.status.executionState == States.RUNNING.value }
            .forEach { execution ->
                val benchmark = benchmarkCRDClient
                    .inNamespace(client.namespace)
                    .list()
                    .items
                    .firstOrNull { it.metadata.name == execution.spec.benchmark }

                if (benchmark != null) {
                    execution.spec.name = execution.metadata.name
                    benchmark.spec.name = benchmark.metadata.name
                    Shutdown(execution.spec, benchmark.spec).start()
                } else {
                    logger.error {
                        "Execution with state ${States.RUNNING.value} was found, but no corresponding benchmark. " +
                                "Could not initialize cluster." }
                }


            }
        }

    private  fun clearByLabel() {
        this.client.services().withLabel("app.kubernetes.io/created-by=theodolite").delete()
        this.client.apps().deployments().withLabel("app.kubernetes.io/created-by=theodolite").delete()
        this.client.apps().statefulSets().withLabel("app.kubernetes.io/created-by=theodolite").delete()
        this.client.configMaps().withLabel("app.kubernetes.io/created-by=theodolite").delete()

        val serviceMonitors = JSONObject(
            this.client.customResource(serviceMonitorContext)
                .list(client.namespace, mapOf(Pair("app.kubernetes.io/created-by", "theodolite")))
        )
            .getJSONArray("items")

        (0 until serviceMonitors.length())
            .map { serviceMonitors.getJSONObject(it).getJSONObject("metadata").getString("name") }
            .forEach { this.client.customResource(serviceMonitorContext).delete(client.namespace, it) }
    }
}