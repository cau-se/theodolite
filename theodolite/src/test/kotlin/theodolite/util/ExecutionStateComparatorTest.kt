package theodolite.util

import io.quarkus.test.junit.QuarkusTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import theodolite.execution.operator.ExecutionCRDummy
import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.States


@QuarkusTest
class ExecutionStateComparatorTest {

    @Test
    fun testCompare() {
        val comparator = ExecutionStateComparator(States.RESTART)
        val execution1 = ExecutionCRDummy("dummy1", "default-benchmark")
        val execution2 = ExecutionCRDummy("dummy2", "default-benchmark")
        execution1.getStatus().executionState = States.RESTART.value
        execution2.getStatus().executionState = States.PENDING.value
        val list = listOf(execution2.getCR(), execution1.getCR())


        assertEquals(
            list.reversed(),
            list.sortedWith(comparator)
        )
    }

}