package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import theodolite.execution.operator.EventCreator

@QuarkusTest
class testTest {

    @Test
    fun testEventCreator() {
        val creator = EventCreator()
        creator.createEvent(
            executionName = "theodolite-example-execution",
            type = "NORMAL",
            message = "test-event",
            reason = "test-reason"
        )
    }
}