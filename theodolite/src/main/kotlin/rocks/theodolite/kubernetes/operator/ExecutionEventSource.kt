package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.processing.event.source.inbound.SimpleInboundEventSource
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

/**
 * Inbound event source that lets [RunnerCoordinator] trigger a reconcile of a specific
 * [ExecutionCRD] whenever its in-memory runtime state changes.
 *
 * Delegates to [SimpleInboundEventSource] which already provides [propagateEvent].
 */
class ExecutionEventSource : SimpleInboundEventSource<ExecutionCRD>()
