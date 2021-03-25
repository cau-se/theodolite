package theodolite.execution

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedInformer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import java.lang.Thread.sleep
import java.util.Queue
import java.util.LinkedList

private val logger = KotlinLogging.logger {}


class TheodoliteController(
    val client: NamespacedKubernetesClient,
    val informerBenchmarkExecution: SharedInformer<BenchmarkExecution>,
    val informerBenchmarkType: SharedInformer<KubernetesBenchmark>,
    val executionContext: CustomResourceDefinitionContext
) {
    lateinit var executor: TheodoliteExecutor
    val executionsQueue: Queue<BenchmarkExecution> = LinkedList<BenchmarkExecution>()
    val benchmarks: MutableMap<String, KubernetesBenchmark> = HashMap()

    /**
     * Adds the EventHandler to kubernetes
     */
    fun create() {
        informerBenchmarkExecution.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(execution: BenchmarkExecution) {
                logger.info { "Add new execution ${execution.metadata.name} to queue" }
                execution.name = execution.metadata.name
                executionsQueue.add(execution)

            }

            override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
                logger.info { "Update execution ${oldExecution.metadata.name}" }
                if (::executor.isInitialized && executor.getExecution().name == newExecution.metadata.name) {
                    logger.info { "restart current benchmark with new version" }
                    executor.stop()
                    executor = TheodoliteExecutor(config = newExecution, kubernetesBenchmark = executor.getBenchmark())
                    executor.run()
                } else {
                    onDelete(oldExecution, false)
                    onAdd(newExecution)
                }
            }

            override fun onDelete(execution: BenchmarkExecution, b: Boolean) {
                logger.info { "Delete execution ${execution.metadata.name} from queue" }
                executionsQueue.removeIf{e -> e.name == execution.metadata.name}
                if (::executor.isInitialized && executor.getExecution().name == execution.metadata.name) {
                    executor.stop()
                    logger.info { "Current benchmark stopped" }
                }
            }
        })

        informerBenchmarkType.addEventHandler(object : ResourceEventHandler<KubernetesBenchmark> {
            override fun onAdd(benchmark: KubernetesBenchmark) {
                logger.info { "Add new benchmark ${benchmark.name}" }
                benchmark.name = benchmark.metadata.name
                benchmarks[benchmark.name] = benchmark
            }

            override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
                logger.info { "Update benchmark ${newBenchmark.metadata.name}" }
                if (::executor.isInitialized && executor.getBenchmark().name == oldBenchmark.metadata.name) {
                    logger.info { "restart current benchmark with new version" }
                    executor.stop()
                    executor = TheodoliteExecutor(config = executor.getExecution(), kubernetesBenchmark = newBenchmark)
                    executor.run()
                } else {
                    onAdd(newBenchmark)
                }
            }

            override fun onDelete(benchmark: KubernetesBenchmark, b: Boolean) {
                logger.info { "Delete benchmark ${benchmark.metadata.name}" }
                benchmarks.remove(benchmark.metadata.name)
                if(::executor.isInitialized && executor.getBenchmark().name == benchmark.metadata.name) {
                    executor.stop()
                    logger.info { "Current benchmark stopped" }
                }
            }
        })
    }

    fun run() {
        while (true) {
            try {
                reconcile()
                logger.info { "Theodolite is waiting for new jobs" }
                sleep(1000)
            } catch (e: InterruptedException) {
                logger.error { "Execution interrupted with error: $e" }
            }
        }
    }
    @Synchronized
    private fun reconcile() {
        while(executionsQueue.isNotEmpty()
            && ((this::executor.isInitialized && !executor.isRunning) || !this::executor.isInitialized)) {
            val execution = executionsQueue.peek()
            val benchmark = benchmarks[execution.benchmark]

            if (benchmark == null) {
                logger.debug { "No benchmark found for execution ${execution.benchmark}" }
            } else {
                logger.info { "Start execution ${execution.name} with benchmark ${benchmark.name}" }
                executor = TheodoliteExecutor(config = execution, kubernetesBenchmark = benchmark)
                executor.run()
                // wait until executions is deleted
                try {
                    client.customResource(executionContext).delete(client.namespace, execution.metadata.name)
                    while (executionsQueue.contains(execution)) {
                        logger.info { "sleep" }
                        sleep(1000)
                    }
                } catch (e: Exception) {
                    logger.error { "Error while delete current execution" }
                }
                logger.info { "Execution is finally stopped for execution ${execution.name}" }
            }
        }
    }
}
