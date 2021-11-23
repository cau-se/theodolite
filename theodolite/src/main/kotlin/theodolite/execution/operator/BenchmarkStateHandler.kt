package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import theodolite.model.crd.*

class BenchmarkStateHandler(val client: NamespacedKubernetesClient) :
    AbstractStateHandler<BenchmarkCRD, KubernetesBenchmarkList, ExecutionStatus>(
        client = client,
        crd = BenchmarkCRD::class.java,
        crdList = KubernetesBenchmarkList::class.java
    ) {

    private fun getBenchmarkResourceState() = { cr: BenchmarkCRD -> cr.status.resourceSetsState }

    fun setResourceSetState(resourceName: String, status: BenchmarkStates): Boolean {
        setState(resourceName) { cr -> cr.status.resourceSetsState = status.value; cr }
        return blockUntilStateIsSet(resourceName, status.value, getBenchmarkResourceState())
    }

    fun getResourceSetState(resourceName: String): ExecutionStates {
        val status = this.getState(resourceName, getBenchmarkResourceState())
        return if (status.isNullOrBlank()) {
            ExecutionStates.NO_STATE
        } else {
            ExecutionStates.values().first { it.value == status }
        }
    }
}