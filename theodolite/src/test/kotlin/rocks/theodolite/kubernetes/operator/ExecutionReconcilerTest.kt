package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRDummy
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import java.time.Instant

@QuarkusTest
internal class ExecutionReconcilerTest {

    private fun reconcilerWith(coordinator: RunnerCoordinator): ExecutionReconciler {
        val reconciler = ExecutionReconciler()
        reconciler.coordinator = coordinator
        // Open by default so existing tests exercise reconcile() as if this were the leader.
        reconciler.readiness = OperatorReadiness().apply { open() }
        return reconciler
    }

    private fun context(): Context<ExecutionCRD> = mock()

    // ---- OperatorReadiness gate -----------------------------------------------------------

    @Test
    fun `reconcile does nothing and reschedules while the operator readiness gate is closed`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE
        val coordinator = mock<RunnerCoordinator>()
        val reconciler = reconcilerWith(coordinator)
        reconciler.readiness = OperatorReadiness() // closed: simulates a non-leader replica

        val result = reconciler.reconcile(execution, context())

        assertTrue(result.isNoUpdate)
        assertEquals(2000L, result.scheduleDelay.get())
        assertEquals(ExecutionState.NO_STATE, execution.status.executionState)
        verify(coordinator, never()).triggerSelection()
    }

    @Test
    fun `only the instance whose readiness gate is open selects and starts an execution`() {
        // Simulates a leader and a non-leader replica reconciling the same execution against the
        // same cluster state: only the "leader" (gate open) may trigger selection or mutate a
        // status; the "non-leader" (gate closed) must be a complete no-op.
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE

        val leaderCoordinator = mock<RunnerCoordinator>()
        val leader = reconcilerWith(leaderCoordinator) // readiness open by default

        val nonLeaderCoordinator = mock<RunnerCoordinator>()
        val nonLeader = reconcilerWith(nonLeaderCoordinator)
        nonLeader.readiness = OperatorReadiness()

        val nonLeaderResult = nonLeader.reconcile(execution, context())
        assertTrue(nonLeaderResult.isNoUpdate)
        assertEquals(ExecutionState.NO_STATE, execution.status.executionState)
        verify(nonLeaderCoordinator, never()).triggerSelection()

        val leaderResult = leader.reconcile(execution, context())
        assertFalse(leaderResult.isNoUpdate)
        assertEquals(0L, leaderResult.scheduleDelay.get())
        assertEquals(ExecutionState.PENDING, execution.status.executionState)
        verify(leaderCoordinator, never()).triggerSelection()
    }

    @Test
    fun `reconcile assigns initial PENDING state to a new execution`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE
        val coordinator = mock<RunnerCoordinator>()

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertFalse(result.isNoUpdate)
        assertEquals(ExecutionState.PENDING, execution.status.executionState)
    }

    @Test
    fun `reconcile reschedules immediately after setting initial PENDING state`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.NO_STATE
        val coordinator = mock<RunnerCoordinator>()

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertEquals(0L, result.scheduleDelay.get())
        verify(coordinator, never()).triggerSelection()
    }

    @Test
    fun `reconcile persists a pending FINISHED completion without clearing it yet`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-01T00:05:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { completionFor("exec") }
                .thenReturn(RunnerCoordinator.Completion(ExecutionState.FINISHED, start, end))
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertFalse(result.isNoUpdate)
        assertEquals(ExecutionState.FINISHED, execution.status.executionState)
        assertEquals(start.toString(), execution.status.startTime?.time)
        assertEquals(end.toString(), execution.status.completionTime?.time)
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
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
            on { activeStartTime() }.thenReturn(start)
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        assertEquals(ExecutionState.RUNNING, execution.status.executionState)
        assertEquals(start.toString(), execution.status.startTime?.time)
        // This first RUNNING patch must reschedule too (not just later reconciles): it is a
        // status-only patch (no `metadata.generation` bump), so without an explicit reschedule
        // here nothing would ever call reconcile() again for this resource.
        assertFalse(result.isNoUpdate)
        assertEquals(1000L, result.scheduleDelay.get())
    }

    @Test
    fun `reconcile re-patches and reschedules to refresh duration once RUNNING is set`() {
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.RUNNING
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
        }

        val result = reconcilerWith(coordinator).reconcile(execution, context())

        // No status field changes, but the status is still patched and rescheduled so
        // `executionDuration` -- which kubectl surfaces via a printer column but cannot compute
        // itself -- keeps refreshing for as long as the execution stays RUNNING.
        assertFalse(result.isNoUpdate)
        assertEquals(1000L, result.scheduleDelay.get())
    }

    @Test
    fun `reconcile keeps re-patching and rescheduling across repeated invocations while RUNNING`() {
        // Traces the self-sustaining loop end to end by manually simulating what JOSDK does with
        // the returned `rescheduleAfter`: invoke reconcile() again with the same RUNNING resource.
        // Regression test for a bug where the *first* RUNNING patch didn't reschedule, so the loop
        // never started and `executionDuration` stayed frozen at its initial value.
        val execution = ExecutionCRDummy("exec", "bench").getCR()
        execution.status.executionState = ExecutionState.PENDING
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val coordinator = mock<RunnerCoordinator> {
            on { activeExecutionName() }.thenReturn("exec")
            on { activeStartTime() }.thenReturn(start)
        }
        val reconciler = reconcilerWith(coordinator)

        // Round 1: PENDING -> RUNNING.
        val first = reconciler.reconcile(execution, context())
        assertFalse(first.isNoUpdate)
        assertEquals(1000L, first.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, execution.status.executionState)

        // Round 2: simulates the framework re-invoking reconcile() after the scheduled delay,
        // passing the now-RUNNING resource back in. Must still be rescheduled -- this is the
        // branch that previously existed but was unreachable because round 1 never rescheduled.
        val second = reconciler.reconcile(execution, context())
        assertFalse(second.isNoUpdate)
        assertEquals(1000L, second.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, execution.status.executionState)

        // Round 3: the loop must keep going indefinitely, not just once more.
        val third = reconciler.reconcile(execution, context())
        assertFalse(third.isNoUpdate)
        assertEquals(1000L, third.scheduleDelay.get())
        assertEquals(ExecutionState.RUNNING, execution.status.executionState)
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
