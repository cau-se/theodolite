package theodolite.execution.operator

import io.fabric8.kubernetes.client.KubernetesClient
import theodolite.model.crd.BenchmarkExecutionList
import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.ExecutionStatus
import theodolite.model.crd.States
import java.lang.Thread.sleep
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ExecutionStateHandler(val client: KubernetesClient):
    AbstractStateHandler<ExecutionCRD, BenchmarkExecutionList, ExecutionStatus >(
        client = client,
        crd = ExecutionCRD::class.java,
        crdList = BenchmarkExecutionList::class.java
    ) {

    private var runExecutionDurationTimer: AtomicBoolean = AtomicBoolean(false)

    private fun getExecutionLambda() = { cr: ExecutionCRD -> cr.status.executionState }

    private fun getDurationLambda() = { cr: ExecutionCRD -> cr.status.executionDuration }

    fun setExecutionState(resourceName: String, status: States): Boolean {
        setState(resourceName) {cr -> cr.status.executionState = status.value; cr}
        return blockUntilStateIsSet(resourceName, status.value, getExecutionLambda())
    }

    fun getExecutionState(resourceName: String) : States {
        val status = this.getState(resourceName, getExecutionLambda())
        return if(status.isNullOrBlank()){
            States.NO_STATE
        } else {
            States.values().first { it.value == status }
        }
    }

    fun setDurationState(resourceName: String, duration: Duration): Boolean {
        setState(resourceName) { cr -> cr.status.executionDuration = durationToK8sString(duration); cr }
        return blockUntilStateIsSet(resourceName, durationToK8sString(duration), getDurationLambda())
    }

    fun getDurationState(resourceName: String): String {
        val status = getState(resourceName, getDurationLambda())
        return if (status.isNullOrBlank()) {
            "-"
        } else {
            status
        }
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