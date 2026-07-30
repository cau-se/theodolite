package rocks.theodolite.kubernetes.operator

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Tests for [TheodoliteController].
 *
 * Selection and run logic has moved to [RunnerCoordinator]; see [RunnerCoordinatorTest].
 * These tests verify that the controller correctly delegates to the coordinator.
 */
@QuarkusTest
class ControllerTest {

    @Test
    fun `stop delegates to coordinator`() {
        val coordinator = mock<RunnerCoordinator>()
        val controller = TheodoliteController(
            coordinator = coordinator,
            benchmarkStateChecker = mock()
        )

        controller.stop(restart = false)

        verify(coordinator).stop(restart = false)
    }

    @Test
    fun `stop with restart delegates restart flag to coordinator`() {
        val coordinator = mock<RunnerCoordinator>()
        val controller = TheodoliteController(
            coordinator = coordinator,
            benchmarkStateChecker = mock()
        )

        controller.stop(restart = true)

        verify(coordinator).stop(restart = true)
    }

    @Test
    fun `isExecutionRunning delegates to coordinator isRunning`() {
        val coordinator = mock<RunnerCoordinator>()
        val controller = TheodoliteController(
            coordinator = coordinator,
            benchmarkStateChecker = mock()
        )

        controller.isExecutionRunning("my-execution")

        verify(coordinator).isRunning("my-execution")
    }
}
