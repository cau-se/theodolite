package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import theodolite.model.crd.*

class BenchmarkStateHandler(val client: NamespacedKubernetesClient) :
    AbstractStateHandler<BenchmarkCRD, KubernetesBenchmarkList, ExecutionStatus>(
        client = client,
        crd = BenchmarkCRD::class.java,
        crdList = KubernetesBenchmarkList::class.java
    ) {

    private fun getBenchmarkResourceState() = { cr: BenchmarkCRD -> cr.status.resourceSets }

    fun setResourceSetState(resourceName: String, status: States): Boolean {
        setState(resourceName) { cr -> cr.status.resourceSets = status.value; cr }
        return blockUntilStateIsSet(resourceName, status.value, getBenchmarkResourceState())
    }

    fun getResourceSetState(resourceName: String): States {
        val status = this.getState(resourceName, getBenchmarkResourceState())
        return if (status.isNullOrBlank()) {
            States.NO_STATE
        } else {
            States.values().first { it.value == status }
        }
    }
}