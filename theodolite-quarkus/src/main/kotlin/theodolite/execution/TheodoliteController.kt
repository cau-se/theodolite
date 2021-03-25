package theodolite.execution

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedInformer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

private val logger = KotlinLogging.logger {}


class TheodoliteController(
    val client: NamespacedKubernetesClient,
    val informerBenchmarkExecution: SharedInformer<BenchmarkExecution>,
    val informerBenchmarkType: SharedInformer<KubernetesBenchmark>,
    val executionContext: CustomResourceDefinitionContext
) {
    lateinit var executor: TheodoliteExecutor
    var executionsQueue: Queue<BenchmarkExecution> = LinkedList<BenchmarkExecution>()
    val benchmarks: MutableMap<String, KubernetesBenchmark> = HashMap()

    /**
     * Adds the EventHandler to kubernetes
     */
    fun create() {
        informerBenchmarkExecution.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(execution: BenchmarkExecution) {
                execution.name = execution.metadata.name
                logger.info { "Add new execution ${execution.metadata.name} to queue" }
                executionsQueue.add(execution)

            }

            override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
                logger.info { "Add updated execution to queue" }
                if (::executor.isInitialized && executor.getExecution().name == newExecution.metadata.name) {
                    executor.executor.run = false
                }
                newExecution.name = newExecution.metadata.name
                executionsQueue.removeIf { e -> e.name == newExecution.metadata.name }
                executionsQueue.add(newExecution)
            }

            override fun onDelete(execution: BenchmarkExecution, b: Boolean) {
                logger.info { "Delete execution ${execution.metadata.name} from queue" }
                executionsQueue.removeIf { e -> e.name == execution.metadata.name }
                if (::executor.isInitialized && executor.getExecution().name == execution.metadata.name) {
                    executor.executor.run = false
                    logger.info { "Current benchmark stopped" }
                }
            }
        })

        informerBenchmarkType.addEventHandler(object : ResourceEventHandler<KubernetesBenchmark> {
            override fun onAdd(benchmark: KubernetesBenchmark) {
                benchmark.name = benchmark.metadata.name
                logger.info { "Add new benchmark ${benchmark.name}" }
                benchmarks[benchmark.name] = benchmark
            }

            override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
                logger.info { "Update benchmark ${newBenchmark.metadata.name}" }
                newBenchmark.name = newBenchmark.metadata.name
                if (::executor.isInitialized && executor.getBenchmark().name == oldBenchmark.metadata.name) {
                    executor.executor.run = false
                    val execution =  executor.getExecution()
                    execution.name = execution.name + System.currentTimeMillis()
                    executionsQueue.add(execution)
                } else {
                    onAdd(newBenchmark)
                }
            }

            override fun onDelete(benchmark: KubernetesBenchmark, b: Boolean) {
                logger.info { "Delete benchmark ${benchmark.metadata.name}" }
                benchmarks.remove(benchmark.metadata.name)
                if (::executor.isInitialized && executor.getBenchmark().name == benchmark.metadata.name) {
                    executor.executor.run = false
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
                sleep(2000)
                if (this::executor.isInitialized && !executor.isRunning) {
                }
            } catch (e: InterruptedException) {
                logger.error { "Execution interrupted with error: $e" }
            }
        }
    }

    @Synchronized
    private fun reconcile() {
        while (executionsQueue.isNotEmpty()
            && ((this::executor.isInitialized && !executor.isRunning) || !this::executor.isInitialized)) {
            val execution = executionsQueue.peek()
            val benchmark = benchmarks[execution.benchmark]

            if (benchmark == null) {
                logger.debug { "No benchmark found for execution ${execution.benchmark}" }
                sleep(1000)
            } else {
                runExecution(execution, benchmark)
            }
        }
    }

    @Synchronized
    fun runExecution(execution: BenchmarkExecution, benchmark: KubernetesBenchmark) {
        logger.info { "Start execution ${execution.name} with benchmark ${benchmark.name}" }
        executor = TheodoliteExecutor(config = execution, kubernetesBenchmark = benchmark)
        executor.run()

        if (executionsQueue.contains(execution)) {
            client.customResource(executionContext).delete(client.namespace, execution.metadata.name)
        }
        logger.info { "Execution of ${execution.name} is finally stopped" }
    }
}
