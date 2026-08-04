package rocks.theodolite.kubernetes.operator

import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

/**
 * Shared gate checked by [BenchmarkReconciler] and [ExecutionReconciler] before they act.
 *
 * It stays closed until this instance has won leadership (see [TheodoliteOperator]) and
 * [ClusterSetup] has finished clearing orphaned cluster state, and is closed again as soon as
 * leadership is lost. A non-leader replica never calls [open], so its reconcilers never do
 * anything either — closing the window where an execution could be selected/started before
 * cleanup finishes, by a non-leader instance, or by a former leader that hasn't noticed yet.
 */
@Unremovable
@ApplicationScoped
class OperatorReadiness {

    companion object {
        /** How often a gated-out reconcile is retried while the gate is still closed. */
        val RETRY_INTERVAL: Duration = Duration.ofSeconds(2)
    }

    private val currentTerm = AtomicLong(0)

    @Volatile
    private var ready = false

    fun isReady(): Boolean = ready

    /** Marks the start of a new leadership term; pass the returned token to [open]. */
    fun beginTerm(): Long = currentTerm.incrementAndGet()

    /** Opens the gate for a fresh term; for callers that don't need to guard a longer-running
     *  operation (e.g. cleanup) against a leadership loss in between. */
    fun open() = open(beginTerm())

    /**
     * Opens the gate, but only if [term] is still the current leadership term. A [close] that
     * happened after [beginTerm] returned [term] — e.g. leadership lost while cleanup was still
     * running — bumps the term first, so this late [open] call for the now-stale term is a no-op.
     */
    @Synchronized
    fun open(term: Long) {
        if (term == currentTerm.get()) {
            ready = true
        }
    }

    /** Closes the gate, e.g. on losing leadership; invalidates any in-flight [beginTerm] token. */
    @Synchronized
    fun close() {
        currentTerm.incrementAndGet()
        ready = false
    }
}

