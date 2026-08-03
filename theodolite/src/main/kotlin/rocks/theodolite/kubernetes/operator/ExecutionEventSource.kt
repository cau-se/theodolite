package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.processing.event.Event
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.javaoperatorsdk.operator.processing.event.source.AbstractEventSource

/**
 * Custom event source that lets [RunnerCoordinator] trigger a reconcile of a specific
 * [rocks.theodolite.kubernetes.model.crd.ExecutionCRD] whenever its in-memory runtime state
 * changes (run started, finished, or interrupted).
 *
 * The [ExecutionReconciler] registers a single instance with the coordinator during
 * [io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer.prepareEventSources];
 * the coordinator then calls [propagateEvent] to ask the framework to re-run `reconcile()`,
 * which reads the runtime state and patches the execution's status accordingly.
 */
class ExecutionEventSource : AbstractEventSource() {

    /**
     * Requests a reconcile of the execution identified by [resourceID].
     * No-op if the framework has not yet wired an event handler (i.e. before the operator started).
     */
    fun propagateEvent(resourceID: ResourceID) {
        val handler = eventHandler ?: return
        handler.handleEvent(Event(resourceID))
    }
}
