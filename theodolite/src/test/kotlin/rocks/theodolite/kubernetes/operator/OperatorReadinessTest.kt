package rocks.theodolite.kubernetes.operator

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class OperatorReadinessTest {

    @Test
    fun `isReady is false by default`() {
        assertFalse(OperatorReadiness().isReady())
    }

    @Test
    fun `open sets isReady to true`() {
        val readiness = OperatorReadiness()

        readiness.open()

        assertTrue(readiness.isReady())
    }

    @Test
    fun `close sets isReady back to false`() {
        val readiness = OperatorReadiness()
        readiness.open()

        readiness.close()

        assertFalse(readiness.isReady())
    }

    @Test
    fun `open for a term is a no-op once that term has been closed`() {
        // Simulates leadership lost while cleanup (beginTerm ... open) was still in flight.
        val readiness = OperatorReadiness()
        val term = readiness.beginTerm()
        readiness.close()

        readiness.open(term)

        assertFalse(readiness.isReady())
    }

    @Test
    fun `open for the current term still succeeds despite an earlier unrelated close`() {
        val readiness = OperatorReadiness()
        readiness.close()
        val term = readiness.beginTerm()

        readiness.open(term)

        assertTrue(readiness.isReady())
    }
}
