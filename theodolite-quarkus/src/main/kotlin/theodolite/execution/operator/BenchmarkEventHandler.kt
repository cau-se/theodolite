package theodolite.execution.operator

import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.KubernetesBenchmark
private val logger = KotlinLogging.logger {}

class BenchmarkEventHandler(private val controller: TheodoliteController): ResourceEventHandler<KubernetesBenchmark> {
    override fun onAdd(benchmark: KubernetesBenchmark) {
        benchmark.name = benchmark.metadata.name
        logger.info { "Add new benchmark ${benchmark.name}." }
        this.controller.benchmarks[benchmark.name] = benchmark
    }

    override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
        logger.info { "Update benchmark ${newBenchmark.metadata.name}." }
        newBenchmark.name = newBenchmark.metadata.name
        if (this.controller.isInitialized() &&  this.controller.executor.getBenchmark().name == oldBenchmark.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
        } else {
            onAdd(newBenchmark)
        }
    }

    override fun onDelete(benchmark: KubernetesBenchmark, b: Boolean) {
        logger.info { "Delete benchmark ${benchmark.metadata.name}." }
        this.controller.benchmarks.remove(benchmark.metadata.name)
        if ( this.controller.isInitialized() &&  this.controller.executor.getBenchmark().name == benchmark.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
            logger.info { "Current benchmark stopped." }
        }
    }
}
