package rocks.theodolite.kubernetes.execution.operator

import io.fabric8.kubernetes.api.model.MicroTime
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import java.lang.Thread.sleep
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ExecutionStateHandler(val client: NamespacedKubernetesClient) :
    AbstractStateHandler<ExecutionCRD>(
        client = client,
        crd = ExecutionCRD::class.java
    ) {

    private var runExecutionDurationTimer: AtomicBoolean = AtomicBoolean(false)

    private fun getExecutionLambda() = { cr: ExecutionCRD -> cr.status.executionState.value }

    fun setExecutionState(resourceName: String, status: ExecutionState): Boolean {
        super.setState(resourceName) { cr -> cr.status.executionState = status; cr }
        return blockUntilStateIsSet(resourceName, status.value, getExecutionLambda())
    }

    fun getExecutionState(resourceName: String): ExecutionState {
        val statusString = this.getState(resourceName, getExecutionLambda())
        return ExecutionState.values().first { it.value == statusString }
    }

    private fun updateDurationState(resourceName: String) {
        super.setState(resourceName) { cr -> cr }
    }

    fun startDurationStateTimer(resourceName: String) {
        this.runExecutionDurationTimer.set(true)

        super.setState(resourceName) { cr -> cr.status.completionTime = null; cr }
        super.setState(resourceName) { cr -> cr.status.startTime = MicroTime(Instant.now().toString()); cr }

        Thread {
            while (this.runExecutionDurationTimer.get()) {
                updateDurationState(resourceName)
                sleep(100 * 1)
            }
        }.start()
    }

    @Synchronized
    fun stopDurationStateTimer(resourceName: String) {
        super.setState(resourceName) { cr -> cr.status.completionTime = MicroTime(Instant.now().toString()); cr }
        this.runExecutionDurationTimer.set(false)
        sleep(100 * 2)
    }
}