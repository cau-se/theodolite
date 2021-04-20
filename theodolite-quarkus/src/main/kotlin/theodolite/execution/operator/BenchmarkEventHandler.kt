package theodolite.execution.operator

import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.KubernetesBenchmark

private val logger = KotlinLogging.logger {}

/**
 * Handles adding, updating and deleting KubernetesBenchmarks.
 *
 * @param controller The TheodoliteController that handles the application state
 *
 * @see TheodoliteController
 * @see KubernetesBenchmark
 */
class BenchmarkEventHandler(private val controller: TheodoliteController) : ResourceEventHandler<KubernetesBenchmark> {

    /**
     * Add a KubernetesBenchmark.
     *
     * @param benchmark the KubernetesBenchmark to add
     *
     * @see KubernetesBenchmark
     */
    override fun onAdd(benchmark: KubernetesBenchmark) {
        benchmark.name = benchmark.metadata.name
        logger.info { "Add new benchmark ${benchmark.name}." }
        this.controller.benchmarks[benchmark.name] = benchmark
    }

    /**
     * Update a KubernetesBenchmark.
     *
     * @param oldBenchmark the KubernetesBenchmark to update
     * @param newBenchmark the updated KubernetesBenchmark
     *
     * @see KubernetesBenchmark
     */
    override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
        logger.info { "Update benchmark ${newBenchmark.metadata.name}." }
        newBenchmark.name = newBenchmark.metadata.name
        if (this.controller.isInitialized() && this.controller.executor.getBenchmark().name == oldBenchmark.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
        } else {
            onAdd(newBenchmark)
        }
    }

    /**
     * Delete a KubernetesBenchmark.
     *
     * @param benchmark the KubernetesBenchmark to delete
     *
     * @see KubernetesBenchmark
     */
    override fun onDelete(benchmark: KubernetesBenchmark, b: Boolean) {
        logger.info { "Delete benchmark ${benchmark.metadata.name}." }
        this.controller.benchmarks.remove(benchmark.metadata.name)
        if (this.controller.isInitialized() && this.controller.executor.getBenchmark().name == benchmark.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
            logger.info { "Current benchmark stopped." }
        }
    }
}
