package theodolite.execution

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedInformer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark

private val logger = KotlinLogging.logger {}


class TheodoliteController(val client : NamespacedKubernetesClient,
                           val informerBenchmarkExecution: SharedInformer<BenchmarkExecution>,
                           val informerBenchmarkType : SharedInformer<KubernetesBenchmark> ) {

    var execution : BenchmarkExecution? = null
    var benchmarkType : KubernetesBenchmark? = null

    var executor : TheodoliteExecutor? = null

    var updated : Boolean = true

     public fun create(){

        informerBenchmarkExecution.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(webServer: BenchmarkExecution) {
                execution = webServer
            }

            override fun onUpdate(webServer: BenchmarkExecution, newWebServer: BenchmarkExecution) {
                println("hello there update")
                execution = newWebServer
                updated = true
                shutdown()

            }

            override fun onDelete(webServer: BenchmarkExecution, b: Boolean) {
                println("delted")
                shutdown()
            }
        })

        informerBenchmarkType.addEventHandler(object : ResourceEventHandler<KubernetesBenchmark> {
            override fun onAdd(webServer: KubernetesBenchmark) {
                benchmarkType = webServer
                println("hello there add")
                println(webServer.name)
            }

            override fun onUpdate(webServer: KubernetesBenchmark, newWebServer: KubernetesBenchmark) {
                benchmarkType = newWebServer
                println("hello there update")
                updated = true
                shutdown()


            }

            override fun onDelete(webServer: KubernetesBenchmark, b: Boolean) {
                println("delted")
                println(webServer.name)
                shutdown()
            }
        })
    }

    fun run(){
        while (true){
            try {
                reconcile()
            }
            catch (e: InterruptedException){
                logger.error{"$e "}
            }
        }
    }

    @Synchronized
    private fun reconcile() {
        val localExecution  = this.execution
        val localType  = this.benchmarkType
        if(localType is KubernetesBenchmark && localExecution is BenchmarkExecution  && updated){

            executor = TheodoliteExecutor(config= localExecution,kubernetesBenchmark = localType)
            executor!!.run()
            updated = false
        }
    }

    private fun shutdown(){

    }
}