package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState

class ExecutionStateHandler(val client: NamespacedKubernetesClient) :
    AbstractStateHandler<ExecutionCRD>(
        client = client,
        crd = ExecutionCRD::class.java
    ) {

    private val executionStateAccessor = { cr: ExecutionCRD -> cr.status.executionState.value }

    fun setExecutionState(resourceName: String, status: ExecutionState): Boolean {
        super.setState(resourceName) { cr -> cr.status.executionState = status; cr }
        return blockUntilStateIsSet(resourceName, status.value, executionStateAccessor)
    }

    fun getExecutionState(resourceName: String): ExecutionState {
        val statusString = this.getState(resourceName, executionStateAccessor)
        return ExecutionState.values().first { it.value == statusString }
    }
}