package theodolite.util

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import theodolite.execution.operator.ExecutionCRDummy
import theodolite.model.crd.ExecutionStates


@QuarkusTest
class ExecutionStateComparatorTest {

    @Test
    fun testCompare() {
        val comparator = ExecutionStateComparator(ExecutionStates.RESTART)
        val execution1 = ExecutionCRDummy("dummy1", "default-benchmark")
        val execution2 = ExecutionCRDummy("dummy2", "default-benchmark")
        execution1.getStatus().executionState = ExecutionStates.RESTART.value
        execution2.getStatus().executionState = ExecutionStates.PENDING.value
        val list = listOf(execution2.getCR(), execution1.getCR())


        assertEquals(
            list.reversed(),
            list.sortedWith(comparator)
        )
    }

}