package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.MicroTime
import io.fabric8.kubernetes.client.KubernetesClient
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Cleaner
import io.javaoperatorsdk.operator.api.reconciler.Constants
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.javaoperatorsdk.operator.processing.event.ResourceID
import io.javaoperatorsdk.operator.processing.event.source.EventSource
import io.javaoperatorsdk.operator.processing.event.source.PrimaryToSecondaryMapper
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource
import jakarta.inject.Inject
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.model.crd.ExecutionStatus
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * How often a still-`RUNNING` execution is re-patched to refresh `status.executionDuration`.
 *
 * That field is a computed value (`now - startTime`) rather than something kubectl can derive on
 * its own, and it is only surfaced through the CRD's `Duration` printer column when the status
 * subresource is actually patched. Without a periodic refresh it would stay frozen at whatever it
 * was when `RUNNING` was first persisted, for the entire lifetime of the run.
 */
private val DURATION_REFRESH_INTERVAL: Duration = Duration.ofSeconds(1)

/**
 * Reconciler for [ExecutionCRD] and the **sole** writer of its status.
 *
 * The reconciler does not run benchmarks itself.  [RunnerCoordinator] owns the runner and the
 * in-memory runtime state; this reconciler reads that state and persists it to the CR status,
 * and asks the coordinator to start/stop runs.  The coordinator triggers reconciles through the
 * registered [ExecutionEventSource] whenever runtime state changes, so status stays in sync
 * without any second writer.
 *
 * Reconcile responsibilities:
 * - Give a freshly created execution its initial [ExecutionState.PENDING] state.
 * - Persist [ExecutionState.RUNNING] (with `startTime`) once the coordinator starts a run.
 * - Persist the terminal [ExecutionState.FINISHED]/[ExecutionState.FAILURE] (with `completionTime`)
 *   the coordinator recorded for a finished run.
 * - Detect a spec change to a running execution (via `metadata.generation`) and ask the coordinator
 *   to stop it so it re-runs with the new spec.
 * - Ask the coordinator to (re)select whenever an eligible execution is observed.
 *
 * A [BenchmarkCRD] [InformerEventSource] links each execution to its benchmark so that a change to
 * the benchmark (e.g. its `resourceSetsState` becoming READY) triggers a reconcile of the dependent
 * executions.  Implementing [Cleaner] registers a finalizer so that deleting a running execution
 * stops the runner cleanly before the CR is removed.
 */
@ControllerConfiguration(namespaces = [Constants.WATCH_CURRENT_NAMESPACE])
class ExecutionReconciler :
    Reconciler<ExecutionCRD>,
    Cleaner<ExecutionCRD>,
    EventSourceInitializer<ExecutionCRD> {

    @Inject
    lateinit var coordinator: RunnerCoordinator

    /**
     * Kubernetes client used to persist status through [patchStatus] rather than through
     * [UpdateControl.patchStatus]. Set automatically by CDI in production; set directly in tests.
     */
    @Inject
    lateinit var client: KubernetesClient

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
        val triggerEventSource = ExecutionEventSource()
        coordinator.registerTrigger(triggerEventSource)
        return EventSourceInitializer.nameEventSources(benchmarkEventSource, triggerEventSource)
    }

    override fun reconcile(
        resource: ExecutionCRD,
        context: Context<ExecutionCRD>
    ): UpdateControl<ExecutionCRD> {
        val name = resource.metadata.name
        logger.debug { "Reconcile execution '$name'." }

        // 1. Give a new execution its initial state.
        if (resource.status.executionState == ExecutionState.NO_STATE) {
            logger.info { "Execution '$name': initial state → ${ExecutionState.PENDING.value}." }
            patchStatus(resource) { it.executionState = ExecutionState.PENDING }
            // Ask the coordinator to (re)select immediately: this status patch only changes
            // `status`, not `metadata.generation`, so the primary informer's resulting event is
            // filtered out by JOSDK's generation-aware processing and would never reach case 4
            // below. Without this, a freshly created execution whose benchmark is already READY
            // is never picked up.
            coordinator.triggerSelection()
            return UpdateControl.noUpdate()
        }

        // 2. Persist a terminal result the coordinator recorded for a finished run.
        //    The completion is retained until the terminal status is observed as persisted on the
        //    resource, and only then cleared. This makes status persistence retry-safe: if the
        //    patch below is rejected (e.g. a transient conflict), the completion survives, the
        //    execution stays excluded from selection, and the next reconcile retries the patch —
        //    instead of losing the result and re-running the already-finished execution.
        val completion = coordinator.completionFor(name)
        if (completion != null) {
            if (resource.status.executionState == completion.state) {
                coordinator.clearCompletion(name)
                return UpdateControl.noUpdate()
            }
            logger.info { "Execution '$name': state → ${completion.state.value}." }
            patchStatus(resource) {
                it.executionState = completion.state
                it.startTime = completion.startTime.toMicroTime()
                it.completionTime = completion.completionTime.toMicroTime()
            }
            return UpdateControl.noUpdate()
        }

        // 3. Handle the currently active run.
        if (coordinator.activeExecutionName() == name) {
            val generation = resource.metadata.generation
            if (generation != null && generation != coordinator.activeGeneration()) {
                logger.info { "Execution '$name': spec changed while running, stopping to re-run." }
                coordinator.stopForRespec(name)
                return UpdateControl.noUpdate()
            }
            if (resource.status.executionState != ExecutionState.RUNNING) {
                val startTime = coordinator.activeStartTime()?.toMicroTime()
                logger.info { "Execution '$name': state → ${ExecutionState.RUNNING.value}." }
                patchStatus(resource) {
                    it.executionState = ExecutionState.RUNNING
                    it.startTime = startTime
                    it.completionTime = null
                }
            } else {
                // Already RUNNING: re-patch (no field changes) purely to refresh the computed
                // `executionDuration`.
                patchStatus(resource) { }
            }
            // Setting/keeping RUNNING is a status-only patch, so it does not bump
            // `metadata.generation` and the resulting primary-informer event is filtered out by
            // JOSDK's generation-aware processing. Without an explicit reschedule here, reconcile
            // would never run again for this resource while it stays RUNNING, and
            // `executionDuration` would stay frozen at whatever it was on this patch.
            return UpdateControl.noUpdate<ExecutionCRD>().rescheduleAfter(DURATION_REFRESH_INTERVAL)
        }

        // 4. Otherwise, if this execution is eligible, ask the coordinator to (re)select.
        val state = resource.status.executionState
        if (state == ExecutionState.PENDING || state == ExecutionState.RUNNING) {
            coordinator.triggerSelection()
        }
        return UpdateControl.noUpdate()
    }

    override fun cleanup(
        resource: ExecutionCRD,
        context: Context<ExecutionCRD>
    ): DeleteControl {
        val name = resource.metadata.name
        if (coordinator.activeExecutionName() == name) {
            logger.info { "Execution '$name' is being deleted while running, stopping the runner." }
            coordinator.stopForDeletion(name)
        }
        coordinator.clearCompletion(name)
        return DeleteControl.defaultDelete()
    }

    /**
     * Applies [mutate] to the execution's status and persists it via the fabric8 client rather than
     * through [UpdateControl.patchStatus]. The bundled operator-sdk builds the status patch as a
     * diff over the whole resource (metadata + spec + status), which the API server rejects with 422
     * on the /status subresource. Fetching the current resource and patching only its status sends a
     * status-only request.
     *
     * TODO: remove this workaround and go back to [UpdateControl.patchStatus] once the operator-sdk
     *  is upgraded to a version based on JOSDK >= 5.1.4 (see
     *  operator-framework/java-operator-sdk#2943). Note: `quarkus.operator-sdk.enable-ssa=true` does
     *  not help on the current version, as it only affects primary-resource patches, not the status.
     */
    private fun patchStatus(resource: ExecutionCRD, mutate: (ExecutionStatus) -> Unit) {
        val crdClient = client.resources(ExecutionCRD::class.java).inNamespace(resource.metadata.namespace)
        val current = crdClient.withName(resource.metadata.name).get() ?: return
        mutate(current.status)
        crdClient.withName(resource.metadata.name).patchStatus(current)
    }
}

private fun Instant.toMicroTime(): MicroTime = MicroTime(this.toString())
