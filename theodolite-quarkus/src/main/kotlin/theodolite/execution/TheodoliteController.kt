package theodolite.execution

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedInformer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark

private val logger = KotlinLogging.logger {}


class TheodoliteController(
    val client: NamespacedKubernetesClient,
    val informerBenchmarkExecution: SharedInformer<BenchmarkExecution>,
    val informerBenchmarkType: SharedInformer<KubernetesBenchmark>
) {
    var execution: BenchmarkExecution? = null
    var benchmarkType: KubernetesBenchmark? = null
    var executor: TheodoliteExecutor? = null
    var updated: Boolean = true

    /**
     * Adds the EventHandler to kubernetes
     */
    fun create() {

        informerBenchmarkExecution.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(benchmarkExecution: BenchmarkExecution) {
                execution = benchmarkExecution
            }

            override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
                execution = newExecution
                updated = true
                shutdown()
            }

            override fun onDelete(benchmarkExecution: BenchmarkExecution, b: Boolean) {
                shutdown()
            }
        })

        informerBenchmarkType.addEventHandler(object : ResourceEventHandler<KubernetesBenchmark> {
            override fun onAdd(kubernetesBenchmark: KubernetesBenchmark) {
                benchmarkType = kubernetesBenchmark
            }

            override fun onUpdate(oldBenchmark: KubernetesBenchmark, newBenchmark: KubernetesBenchmark) {
                benchmarkType = newBenchmark
                updated = true
                shutdown()
            }

            override fun onDelete(kubernetesBenchmark: KubernetesBenchmark, b: Boolean) {
                shutdown()
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
        val localExecution = this.execution
        val localType = this.benchmarkType

        if (localType is KubernetesBenchmark && localExecution is BenchmarkExecution && updated) {
            executor = TheodoliteExecutor(config = localExecution, kubernetesBenchmark = localType)
            executor!!.run()
            updated = false
        }
    }

    private fun shutdown() {
        Shutdown(benchmarkExecution = execution!!, benchmark = benchmarkType!!).run()
    }
}
