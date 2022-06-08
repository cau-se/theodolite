package rocks.theodolite.kubernetes.model.crd

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


@QuarkusTest
class ExecutionStateComparatorTest {

    @Test
    fun testCompare() {
        val comparator = ExecutionStateComparator(ExecutionState.RESTART)
        val execution1 = ExecutionCRDummy("dummy1", "default-benchmark")
        val execution2 = ExecutionCRDummy("dummy2", "default-benchmark")
        execution1.getStatus().executionState = ExecutionState.RESTART
        execution2.getStatus().executionState = ExecutionState.PENDING
        val list = listOf(execution2.getCR(), execution1.getCR())

        assertEquals(
            list.reversed(),
            list.sortedWith(comparator)
        )
    }

}