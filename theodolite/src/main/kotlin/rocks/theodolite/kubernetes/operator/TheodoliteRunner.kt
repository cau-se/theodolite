package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.kubernetes.TheodoliteExecutor
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

/**
 * Manages the execution of Theodolite benchmarks on a dedicated single-thread executor.
 *
 * Provides the run/stop/isRunning interface used by [TheodoliteController].
 * Using a single-thread executor ensures at most one execution is active at any point.
 *
 * @param executorFactory factory for creating [TheodoliteExecutor] instances; override in tests.
 */
class TheodoliteRunner(
    private val executorFactory: (BenchmarkExecution, KubernetesBenchmark, NamespacedKubernetesClient) -> TheodoliteExecutor =
        { execution, benchmark, client -> TheodoliteExecutor(execution, benchmark, client) }
) {
    private val threadExecutor = Executors.newSingleThreadExecutor()

    @Volatile
    private var currentExecutor: TheodoliteExecutor? = null

    /**
     * Submits a benchmark execution to the dedicated runner thread and blocks until it completes.
     *
     * [onComplete] is invoked on the runner thread after [TheodoliteExecutor.setupAndRunExecution]
     * returns, regardless of whether the execution succeeded or threw.
     *
     * @throws Exception if [TheodoliteExecutor.setupAndRunExecution] throws; [ExecutionException]
     *     is unwrapped so callers see the original exception type.
     */
    fun run(
        execution: BenchmarkExecution,
        benchmark: KubernetesBenchmark,
        client: NamespacedKubernetesClient,
        onComplete: () -> Unit = {}
    ) {
        val future = threadExecutor.submit {
            val executor = executorFactory(execution, benchmark, client)
            currentExecutor = executor
            try {
                executor.setupAndRunExecution()
            } finally {
                currentExecutor = null
                onComplete()
            }
        }
        try {
            future.get()
        } catch (e: ExecutionException) {
            val cause = e.cause
            if (cause is Exception) throw cause
            throw RuntimeException(cause)
        }
    }

    /**
     * Signals the currently running execution to stop.
     * Has no effect if no execution is currently running.
     */
    @Synchronized
    fun stop() {
        currentExecutor?.stop()
    }

    /**
     * Returns true if the execution with the given name is currently running.
     */
    fun isRunning(executionName: String): Boolean {
        return currentExecutor?.getExecution()?.name == executionName
    }

    /**
     * Returns the currently running execution, or null if none is running.
     */
    fun getExecution(): BenchmarkExecution? = currentExecutor?.getExecution()
}
