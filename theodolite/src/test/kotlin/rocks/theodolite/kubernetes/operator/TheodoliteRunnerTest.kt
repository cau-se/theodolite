package rocks.theodolite.kubernetes.operator

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import rocks.theodolite.kubernetes.TheodoliteExecutor
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@QuarkusTest
class TheodoliteRunnerTest {

    @Test
    fun `isRunning returns false when no execution is running`() {
        val runner = TheodoliteRunner()
        assertFalse(runner.isRunning("test-execution"))
    }

    @Test
    fun `getExecution returns null when no execution is running`() {
        val runner = TheodoliteRunner()
        assertNull(runner.getExecution())
    }

    @Test
    fun `run calls setupAndRunExecution on the executor`() {
        val mockExecution = mock<BenchmarkExecution>()
        val mockExecutor = mock<TheodoliteExecutor> {
            on { getExecution() } doAnswer { mockExecution }
        }
        val runner = TheodoliteRunner { _, _, _ -> mockExecutor }

        runner.run(mockExecution, mock(), mock())

        verify(mockExecutor).setupAndRunExecution()
    }

    @Test
    fun `isRunning returns false after run completes`() {
        val mockExecution = mock<BenchmarkExecution> {
            on { name } doAnswer { "test-execution" }
        }
        val mockExecutor = mock<TheodoliteExecutor> {
            on { getExecution() } doAnswer { mockExecution }
        }
        val runner = TheodoliteRunner { _, _, _ -> mockExecutor }

        runner.run(mockExecution, mock(), mock())

        assertFalse(runner.isRunning("test-execution"))
    }

    @Test
    fun `start invokes beforeRun and reports success via onComplete`() {
        val done = CountDownLatch(1)
        val order = mutableListOf<String>()
        val mockExecutor = mock<TheodoliteExecutor> {
            on { setupAndRunExecution() } doAnswer { order.add("run"); Unit }
        }
        val runner = TheodoliteRunner { _, _, _ -> mockExecutor }

        var reportedError: Throwable? = Exception("unset")
        runner.start(mock(), mock(), mock(), beforeRun = { order.add("before") }) { error ->
            reportedError = error
            done.countDown()
        }

        assert(done.await(5, TimeUnit.SECONDS)) { "onComplete was not called within timeout" }
        assertNull(reportedError)
        assertEquals(listOf("before", "run"), order)
    }

    @Test
    fun `start reports the failure cause via onComplete`() {
        val done = CountDownLatch(1)
        val failure = RuntimeException("boom")
        val mockExecutor = mock<TheodoliteExecutor> {
            on { setupAndRunExecution() } doAnswer { throw failure }
        }
        val runner = TheodoliteRunner { _, _, _ -> mockExecutor }

        var reportedError: Throwable? = null
        runner.start(mock(), mock(), mock()) { error ->
            reportedError = error
            done.countDown()
        }

        assert(done.await(5, TimeUnit.SECONDS)) { "onComplete was not called within timeout" }
        assertEquals(failure, reportedError)
    }

    @Test
    fun `start skips beforeRun and the executor when cancelled before it begins`() {
        val done = CountDownLatch(1)
        var executorCreated = false
        var beforeRunInvoked = false
        val runner = TheodoliteRunner { _, _, _ ->
            executorCreated = true
            mock<TheodoliteExecutor>()
        }

        var reportedError: Throwable? = Exception("unset")
        runner.start(
            mock(), mock(), mock(),
            beforeRun = { beforeRunInvoked = true },
            isCancelled = { true }
        ) { error ->
            reportedError = error
            done.countDown()
        }

        assert(done.await(5, TimeUnit.SECONDS)) { "onComplete was not called within timeout" }
        assertFalse(executorCreated) { "executor must not be created for a cancelled run" }
        assertFalse(beforeRunInvoked) { "beforeRun must not run for a cancelled run" }
        assertNull(reportedError)
    }

    @Test
    fun `stop delegates to the current executor while running`() {
        val runnerStarted = CountDownLatch(1)
        val allowComplete = CountDownLatch(1)
        val mockExecution = mock<BenchmarkExecution>()
        val mockExecutor = mock<TheodoliteExecutor> {
            on { getExecution() } doAnswer { mockExecution }
            on { setupAndRunExecution() } doAnswer {
                runnerStarted.countDown()
                allowComplete.await()
            }
        }
        val runner = TheodoliteRunner { _, _, _ -> mockExecutor }

        val runThread = Thread { runner.run(mockExecution, mock(), mock()) }
        runThread.start()

        assert(runnerStarted.await(5, TimeUnit.SECONDS)) { "Runner did not start within timeout" }
        runner.stop()
        allowComplete.countDown()
        runThread.join(5000)

        verify(mockExecutor).stop()
    }
}
