package theodolite.execution

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedInformer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import java.util.Queue
import java.util.LinkedList

private val logger = KotlinLogging.logger {}


class TheodoliteController(
    val client: NamespacedKubernetesClient,
    val informerBenchmarkExecution: SharedInformer<BenchmarkExecution>,
    val informerBenchmarkType: SharedInformer<KubernetesBenchmark>
) {
    lateinit var executor: TheodoliteExecutor
    val self = this
    val executionsQueue: Queue<BenchmarkExecution> = LinkedList<BenchmarkExecution>()
    val benchmarks: MutableMap<String, KubernetesBenchmark> = HashMap()

    /**
     * Adds the EventHandler to kubernetes
     */
    fun create() {

        informerBenchmarkExecution.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(benchmarkExecution: BenchmarkExecution) {
                executionsQueue.add(benchmarkExecution)
            }

            override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
                if (executor.getExecution().name == newExecution.name) {
                    executor.stop()
                    executor = TheodoliteExecutor(config = newExecution, kubernetesBenchmark = executor.getBenchmark())
                    executor.run()
                } else {
                    executionsQueue.remove(oldExecution)
                    onAdd(newExecution)
                }
            }

            override fun onDelete(execution: BenchmarkExecution, b: Boolean) {
                if (executor.getExecution().name == execution.name) {
                    executor.stop()
                } else {
                    executionsQueue.remove(execution)
                }
            }
        })

        informerBenchmarkType.addEventHandler(object : ResourceEventHandler<KubernetesBenchmark> {
            override fun onAdd(kubernetesBenchmark: KubernetesBenchmark) {
                benchmarks[kubernetesBenchmark.name] = kubernetesBenchmark
            }

            override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
                onAdd(newBenchmark)
                if (executor.getBenchmark().name == oldBenchmark.name) {
                    executor.stop()
                    executor = TheodoliteExecutor(config = executor.getExecution(), kubernetesBenchmark = newBenchmark)
                    executor.run()
                }
            }

            override fun onDelete(benchmark: KubernetesBenchmark, b: Boolean) {
                benchmarks.remove(benchmark.name)
                if(executor.getBenchmark().name == benchmark.name) {
                    executor.stop()
                }
            }
        })
    }

    fun run() {
        while (true) {
            try {
                reconcile()
            } catch (e: InterruptedException) {
                logger.error { "$e" }
            }
        }
    }

    @Synchronized
    private fun reconcile() {
        while(executionsQueue.isNotEmpty()) {
            val execution = executionsQueue.poll()
            val benchmark = benchmarks[execution.name]
            if (benchmark == null) {
                logger.error { "No benchmark found for execution ${execution.name}" }
                executionsQueue.add(execution)
            } else {
                if ((this::executor.isInitialized && !executor.isRunning) || !this::executor.isInitialized) {
                    executor = TheodoliteExecutor(config = execution, kubernetesBenchmark = benchmark)
                    executor.run()
                }
            }
        }
    }

}
