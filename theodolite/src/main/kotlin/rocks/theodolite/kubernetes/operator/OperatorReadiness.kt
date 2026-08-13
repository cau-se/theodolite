package rocks.theodolite.kubernetes.operator

import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration

/**
 * Shared gate checked by [BenchmarkReconciler] and [ExecutionReconciler] before they act.
 *
 * It stays closed until JOSDK's leader-election callback has finished [ClusterSetup]. JOSDK then
 * starts event processing; its leadership-loss callback closes the gate before this replica can
 * perform another reconcile.
 */
@Unremovable
@ApplicationScoped
class OperatorReadiness {

    companion object {
        /** How often a gated-out reconcile is retried while the gate is still closed. */
        val RETRY_INTERVAL: Duration = Duration.ofSeconds(2)
    }

    @Volatile
    private var ready = false

    fun isReady(): Boolean = ready

    fun open() {
        ready = true
    }

    fun close() {
        ready = false
    }
}

