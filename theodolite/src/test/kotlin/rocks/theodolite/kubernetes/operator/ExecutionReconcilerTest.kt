package rocks.theodolite.kubernetes.operator

import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRDummy
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import java.util.Optional

@QuarkusTest
internal class ExecutionReconcilerTest {

    private val reconciler = ExecutionReconciler()

    @Test
    fun `reconcile returns noUpdate when execution is pending and benchmark is ready`() {
        val execution = ExecutionCRDummy("exec1", "bench1").getCR()
        execution.status.executionState = ExecutionState.PENDING

        val benchmark = BenchmarkCRDummy("bench1").getCR()
        benchmark.status.resourceSetsState = BenchmarkState.READY

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.of(benchmark))

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when execution is pending and benchmark is not ready`() {
        val execution = ExecutionCRDummy("exec2", "bench2").getCR()
        execution.status.executionState = ExecutionState.PENDING

        val benchmark = BenchmarkCRDummy("bench2").getCR()
        benchmark.status.resourceSetsState = BenchmarkState.PENDING

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.of(benchmark))

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when execution is in restart state and benchmark is ready`() {
        val execution = ExecutionCRDummy("exec3", "bench3").getCR()
        execution.status.executionState = ExecutionState.RESTART

        val benchmark = BenchmarkCRDummy("bench3").getCR()
        benchmark.status.resourceSetsState = BenchmarkState.READY

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.of(benchmark))

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when execution is finished`() {
        val execution = ExecutionCRDummy("exec4", "bench4").getCR()
        execution.status.executionState = ExecutionState.FINISHED

        val benchmark = BenchmarkCRDummy("bench4").getCR()
        benchmark.status.resourceSetsState = BenchmarkState.READY

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.of(benchmark))

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when referenced benchmark is absent`() {
        val execution = ExecutionCRDummy("exec5", "missing-bench").getCR()
        execution.status.executionState = ExecutionState.PENDING

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.empty())

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when execution state is NO_STATE`() {
        val execution = ExecutionCRDummy("exec6", "bench6").getCR()
        execution.status.executionState = ExecutionState.NO_STATE

        val benchmark = BenchmarkCRDummy("bench6").getCR()
        benchmark.status.resourceSetsState = BenchmarkState.READY

        @Suppress("UNCHECKED_CAST")
        val context: Context<ExecutionCRD> = mock()
        whenever(context.getSecondaryResource(BenchmarkCRD::class.java))
            .thenReturn(Optional.of(benchmark))

        val result = reconciler.reconcile(execution, context)

        assertTrue(result.isNoUpdate)
    }
}
