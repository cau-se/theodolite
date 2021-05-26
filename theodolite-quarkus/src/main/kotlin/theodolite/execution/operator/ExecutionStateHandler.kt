package theodolite.execution.operator

import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import theodolite.model.crd.BenchmarkExecutionList
import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.STATES
import java.lang.Thread.sleep
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ExecutionStateHandler(context: CustomResourceDefinitionContext, val client: KubernetesClient):
    AbstractStateHandler<ExecutionCRD, BenchmarkExecutionList, DoneableExecution>(
        context = context,
        client = client,
        crd = ExecutionCRD::class.java,
        crdList = BenchmarkExecutionList::class.java,
        donableCRD = DoneableExecution::class.java) {

    private var runExecutionDurationTimer: AtomicBoolean = AtomicBoolean(false)

    private fun getExecutionLambda() = { cr: CustomResource ->
        var execState = ""
        if (cr is ExecutionCRD) { execState = cr.status.executionState }
        execState
    }

    private fun getDurationLambda() = { cr: CustomResource ->
        var execState = ""
        if (cr is ExecutionCRD) { execState = cr.status.executionState }
        execState
    }

    fun setExecutionState(resourceName: String, status: STATES): Boolean {
        setState(resourceName) {cr -> if(cr is ExecutionCRD) cr.status.executionState = status.value; cr}
        return blockUntilStateIsSet(resourceName, status.value, getExecutionLambda())
    }

    fun getExecutionState(resourceName: String) : STATES? {
        val status = this.getState(resourceName, getExecutionLambda())
        return  STATES.values().firstOrNull { it.value == status }
    }

    fun setDurationState(resourceName: String, duration: Duration) {
        setState(resourceName) { cr -> if (cr is ExecutionCRD) cr.status.executionDuration = durationToK8sString(duration); cr }
        blockUntilStateIsSet(resourceName, durationToK8sString(duration), getDurationLambda())
    }

    fun getDurationState(resourceName: String): String? {
        return this.getState(resourceName, getDurationLambda())
    }

    private fun durationToK8sString(duration: Duration): String {
        val sec = duration.seconds
        return when {
            sec <= 120 -> "${sec}s" // max 120s
            sec < 60 * 99 -> "${duration.toMinutes()}m" // max 99m
            sec < 60 * 60 * 99 -> "${duration.toHours()}h"   // max 99h
            else -> "${duration.toDays()}d + ${duration.minusDays(duration.toDays()).toHours()}h"
        }
    }

    fun startDurationStateTimer(resourceName: String) {
        this.runExecutionDurationTimer.set(true)
        val startTime = Instant.now().toEpochMilli()
        Thread {
            while (this.runExecutionDurationTimer.get()) {
                val duration = Duration.ofMillis(Instant.now().minusMillis(startTime).toEpochMilli())
                setDurationState(resourceName, duration)
                sleep(100 * 1)
            }
        }.start()
    }

    @Synchronized
    fun stopDurationStateTimer() {
        this.runExecutionDurationTimer.set(false)
        sleep(100 * 2)
    }
}