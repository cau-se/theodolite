package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRDummy
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.model.crd.ExecutionStatus
import java.time.Instant

@QuarkusTest
internal class ExecutionReconcilerTest {

    private companion object {
        const val NAMESPACE = "test"
    }

    /** CRUD-mode mock server used to round-trip status writes (patchStatus workaround). */
    private val server = KubernetesServer(false, true)

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    private fun reconcilerWith(coordinator: RunnerCoordinator): ExecutionReconciler {
        val reconciler = ExecutionReconciler()
        reconciler.coordinator = coordinator
        reconciler.client = server.client
        return reconciler
    }

    /** Persists the execution CR in the mock server so the reconciler can patch its status. */
    private fun createOnServer(execution: ExecutionCRD) {
        execution.metadata.namespace = NAMESPACE
        server.client.resources(ExecutionCRD::class.java)
            .inNamespace(NAMESPACE)
            .resource(execution)
            .create()
    }

    /** Reads the persisted status of the named execution back from the mock server. */
    private fun persistedStatus(name: String): ExecutionStatus =
        server.client.resources(ExecutionCRD::class.java)
            .inNamespace(NAMESPACE)
            .withName(name)
            .get()
            .status

    /** Reads the persisted resource back from the mock server (e.g. to re-feed a reconcile call). */
    private fun persistedResource(name: String): ExecutionCRD =
        server.client.resources(ExecutionCRD::class.java)
            .inNamespace(NAMESPACE)
            .withName(name)
            .get()

    private fun context(): Context<ExecutionCRD> = mock()

    @Test
    fun `reconcile assigns initial PENDING state to a new execution`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE
        createOnServer(execution)
        val coordinator = mock<RunnerCoordinator>()

        reconcilerWith(coordinator).reconcile(execution, context())

        assertEquals(ExecutionState.PENDING, persistedStatus("exec").executionState)
    }

    @Test
    fun `reconcile triggers selection immediately for a newly PENDING execution`() {
        // A status-only patch does not bump `metadata.generation`, so JOSDK's generation-aware
        // processing would filter out the resulting primary-informer event and never call
        // reconcile again for this resource. Selection must therefore be triggered inline here,
        // otherwise an execution whose benchmark is already READY would never be picked up.
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE
        createOnServer(execution)
        val coordinator = mock<RunnerCoordinator>()

        reconcilerWith(coordinator).reconcile(execution, context())

        verify(coordinator).triggerSelection()
    }

    @Test
    fun `reconcile persists a pending FINISHED completion without clearing it yet`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        createOnServer(execution)
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-01T00:05:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { completionFor("exec") }
                .thenReturn(RunnerCoordinator.Completion(ExecutionState.FINISHED, start, end))
        }

        reconcilerWith(coordinator).reconcile(execution, context())

        val persisted = persistedStatus("exec")
        assertEquals(ExecutionState.FINISHED, persisted.executionState)
        assertEquals(start.toString(), persisted.startTime?.time)
        assertEquals(end.toString(), persisted.completionTime?.time)
        // The completion must survive until the terminal status is observed as persisted, so a
        // rejected patch can be retried instead of losing the result.
        verify(coordinator, never()).clearCompletion("exec")
    }

    @Test
    fun `reconcile clears the completion only once the terminal status is persisted`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.FINISHED
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-01T00:05:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { completionFor("exec") }
                .thenReturn(RunnerCoordinator.Completion(ExecutionState.FINISHED, start, end))
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertTrue(result.isNoUpdate)
        verify(coordinator).clearCompletion("exec")
    }

    @Test
    fun `reconcile does not select while a terminal result is pending persistence`() {
        // A rejected status patch leaves the execution RUNNING while the completion is retained.
        // The reconciler must not treat it as an interrupted run and trigger a second selection.
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        createOnServer(execution)
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-01T00:05:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { completionFor("exec") }
                .thenReturn(RunnerCoordinator.Completion(ExecutionState.FAILURE, start, end))
        }

        reconcilerWith(coordinator).reconcile(execution, context())

        verify(coordinator, never()).triggerSelection()
    }

    @Test
    fun `reconcile writes RUNNING and startTime for the active execution`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.PENDING
        createOnServer(execution)
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
            on { activeStartTime() }.thenReturn(start)
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        val persisted = persistedStatus("exec")
        assertEquals(ExecutionState.RUNNING, persisted.executionState)
        assertEquals(start.toString(), persisted.startTime?.time)
        // This first RUNNING patch must reschedule too (not just later no-op reconciles):
        // it is a status-only patch (no `metadata.generation` bump), so without an explicit
        // reschedule here nothing would ever call reconcile() again for this resource.
        assertTrue(result.isNoUpdate)
        assertEquals(1000L, result.scheduleDelay.get())
    }

    @Test
    fun `reconcile re-patches and reschedules to refresh duration once RUNNING is set`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        createOnServer(execution)
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        // Still a status no-op (no field changes), but rescheduled so `executionDuration` --
        // which kubectl surfaces via a printer column but cannot compute itself -- keeps
        // refreshing for as long as the execution stays RUNNING.
        assertTrue(result.isNoUpdate)
        assertEquals(1000L, result.scheduleDelay.get())
    }

    @Test
    fun `reconcile keeps re-patching and rescheduling across repeated invocations while RUNNING`() {
        // Traces the self-sustaining loop end to end by manually simulating what JOSDK does with
        // the returned `rescheduleAfter`: invoke reconcile() again with the freshly persisted
        // resource. Regression test for a bug where the *first* RUNNING patch didn't reschedule,
        // so the loop never started and `executionDuration` stayed frozen at its initial value.
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.PENDING
        createOnServer(execution)
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
            on { activeStartTime() }.thenReturn(start)
        }
        val reconciler = reconcilerWith(coordinator)

        // Round 1: PENDING -> RUNNING.
        val first = reconciler.reconcile(execution, context())
        assertTrue(first.isNoUpdate)
        assertEquals(1000L, first.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, persistedStatus("exec").executionState)

        // The mock server assigns `metadata.generation` on create; keep the mocked "currently
        // active generation" in sync so rounds 2/3 are recognized as the same, unchanged spec
        // instead of spuriously taking the spec-changed/respec branch.
        whenever(coordinator.activeGeneration()).thenReturn(persistedResource("exec").metadata.generation)

        // Round 2: simulates the framework re-invoking reconcile() after the scheduled delay,
        // passing the now-RUNNING resource back in. Must still be rescheduled -- this is the
        // branch that previously existed but was unreachable because round 1 never rescheduled.
        val second = reconciler.reconcile(persistedResource("exec"), context())
        assertTrue(second.isNoUpdate)
        assertEquals(1000L, second.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, persistedStatus("exec").executionState)

        // Round 3: the loop must keep going indefinitely, not just once more.
        val third = reconciler.reconcile(persistedResource("exec"), context())
        assertTrue(third.isNoUpdate)
        assertEquals(1000L, third.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, persistedStatus("exec").executionState)
    }

    @Test
    fun `reconcile stops the active execution when its spec changed`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        execution.metadata.generation = 2L
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
            on { activeGeneration() }.thenReturn(1L)
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertTrue(result.isNoUpdate)
        verify(coordinator).stopForRespec("exec")
    }

    @Test
    fun `reconcile triggers selection for a pending execution that is not active`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.PENDING
        val coordinator = mock<RunnerCoordinator>()

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertTrue(result.isNoUpdate)
        verify(coordinator).triggerSelection()
    }

    @Test
    fun `reconcile does not trigger selection for a finished execution`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.FINISHED
        val coordinator = mock<RunnerCoordinator>()

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertTrue(result.isNoUpdate)
        verify(coordinator, never()).triggerSelection()
    }

    @Test
    fun `cleanup stops the runner when the deleted execution is active`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
        }

        val result = reconcilerWith(coordinator).cleanup(execution, context())

        assertTrue(result.isRemoveFinalizer)
        verify(coordinator).stopForDeletion("exec")
    }

    @Test
    fun `cleanup does not stop the runner when another execution is active`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("other")
        }

        reconcilerWith(coordinator).cleanup(execution, context())

        verify(coordinator, never()).stopForDeletion("exec")
    }
}
