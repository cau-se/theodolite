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
     * Submits a benchmark execution to the dedicated runner thread and returns immediately.
     *
     * [beforeRun] is invoked on the runner thread just before
     * [TheodoliteExecutor.setupAndRunExecution] (e.g. to apply deployment labels).
     * [isCancelled] is checked on the runner thread — while holding the same monitor as [stop] —
     * immediately before the executor is created. If it returns `true` the run is abandoned before
     * any deployment happens ([beforeRun] and the executor are skipped); this closes the window in
     * which a queued run could otherwise deploy resources after a stop/delete request that arrived
     * before the executor existed. Once the executor exists, an in-flight run is stopped via [stop].
     * [onComplete] is invoked on the runner thread once the execution finishes (or is cancelled),
     * receiving the throwable that terminated it or `null` on success/cancellation. The single-thread
     * executor guarantees that a subsequent submission does not start until the current one has fully
     * completed.
     */
    fun start(
        execution: BenchmarkExecution,
        benchmark: KubernetesBenchmark,
        client: NamespacedKubernetesClient,
        beforeRun: () -> Unit = {},
        isCancelled: () -> Boolean = { false },
        onComplete: (Throwable?) -> Unit
    ) {
        threadExecutor.submit {
            var error: Throwable? = null
            try {
                val executor = synchronized(this) {
                    if (isCancelled()) {
                        return@submit
                    }
                    executorFactory(execution, benchmark, client).also { currentExecutor = it }
                }
                try {
                    beforeRun()
                    executor.setupAndRunExecution()
                } finally {
                    synchronized(this) { currentExecutor = null }
                }
            } catch (t: Throwable) {
                error = t
            } finally {
                onComplete(error)
            }
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
