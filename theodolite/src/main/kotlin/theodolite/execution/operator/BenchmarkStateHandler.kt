package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import theodolite.model.crd.*

class BenchmarkStateHandler(val client: NamespacedKubernetesClient) :
    AbstractStateHandler<BenchmarkCRD>(
        client = client,
        crd = BenchmarkCRD::class.java
    ) {

    private fun getBenchmarkResourceState() = { cr: BenchmarkCRD -> cr.status.resourceSetsState.value }

    fun setResourceSetState(resourceName: String, status: BenchmarkState): Boolean {
        setState(resourceName) { cr -> cr.status.resourceSetsState = status; cr }
        return blockUntilStateIsSet(resourceName, status.value, getBenchmarkResourceState())
    }

    fun getResourceSetState(resourceName: String): ExecutionState {
        val status = this.getState(resourceName, getBenchmarkResourceState())
        return if (status.isNullOrBlank()) {
            ExecutionState.NO_STATE
        } else {
            ExecutionState.values().first { it.value == status }
        }
    }
}