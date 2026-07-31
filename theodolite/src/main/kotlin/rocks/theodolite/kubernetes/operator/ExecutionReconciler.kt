package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.javaoperatorsdk.operator.processing.event.source.EventSource
import io.javaoperatorsdk.operator.processing.event.source.PrimaryToSecondaryMapper
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState

private val logger = KotlinLogging.logger {}

/**
 * Passive / observational reconciler for [ExecutionCRD].
 *
 * **Read-only:** always returns [UpdateControl.noUpdate]; no status patches, no finalizers.
 *
 * Wires a [BenchmarkCRD] [InformerEventSource] so that a change to the benchmark
 * referenced by an execution (e.g., its `resourceSetsState` flipping to READY)
 * immediately triggers a reconcile of the dependent executions.  Inside [reconcile]
 * the observed eligibility — whether this execution would be a candidate for selection
 * — is computed and logged for comparison with the selection path in [RunnerCoordinator].
 *
 * The real [ExecutionState] written into the CR is still managed exclusively by
 * [RunnerCoordinator] / [ExecutionStateHandler]; this reconciler observes but does not write.
 */
@ControllerConfiguration
class ExecutionReconciler : Reconciler<ExecutionCRD>, EventSourceInitializer<ExecutionCRD> {

    override fun prepareEventSources(
        context: EventSourceContext<ExecutionCRD>
    ): Map<String, EventSource> {
        val benchmarkEventSource = InformerEventSource(
            InformerConfiguration.from(BenchmarkCRD::class.java, context)
                // Execution → Benchmark: enables getSecondaryResource(BenchmarkCRD) in reconcile.
                .withPrimaryToSecondaryMapper(
                    PrimaryToSecondaryMapper { execution: ExecutionCRD ->
                        try {
                            setOf(ResourceID(execution.spec.benchmark, execution.metadata.namespace))
                        } catch (_: UninitializedPropertyAccessException) {
                            emptySet()
                        }
                    }
                )
                // Benchmark → Executions: triggers reconcile of dependent executions when
                // a benchmark changes (e.g., resourceSetsState becomes READY).
                .withSecondaryToPrimaryMapper(
                    SecondaryToPrimaryMapper { benchmark: BenchmarkCRD ->
                        context.primaryCache.list().toList()
                            .filter { execution ->
                                try {
                                    execution.spec.benchmark == benchmark.metadata.name
                                } catch (_: UninitializedPropertyAccessException) {
                                    false
                                }
                            }
                            .map { ResourceID.fromResource(it) }
                            .toSet()
                    }
                )
                .build(),
            context
        )
        return EventSourceInitializer.nameEventSources(benchmarkEventSource)
    }

    override fun reconcile(
        resource: ExecutionCRD,
        context: Context<ExecutionCRD>
    ): UpdateControl<ExecutionCRD> {
        val name = resource.metadata.name
        logger.debug { "Reconcile execution $name." }

        val executionState = resource.status.executionState
        val benchmark = context.getSecondaryResource(BenchmarkCRD::class.java).orElse(null)

        val benchmarkReady = benchmark?.status?.resourceSetsState == BenchmarkState.READY
        val stateEligible = executionState == ExecutionState.PENDING ||
            executionState == ExecutionState.RESTART
        val observedEligibility = stateEligible && benchmarkReady

        logger.info {
            "Execution '$name': observedEligibility=$observedEligibility" +
                " (state=$executionState, benchmarkReady=$benchmarkReady)"
        }

        // Read-only: RunnerCoordinator / ExecutionStateHandler remain the sole status writers.
        return UpdateControl.noUpdate()
    }
}
