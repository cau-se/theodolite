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

}
