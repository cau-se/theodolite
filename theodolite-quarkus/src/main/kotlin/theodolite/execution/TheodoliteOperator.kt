package theodolite.execution

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.*
import io.fabric8.kubernetes.internal.KubernetesDeserializer




private var DEFAULT_NAMESPACE = "default"
private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteCRDExecutor")
object TheodoliteCRDExecutor {
    @JvmStatic
    fun main(args: Array<String>) {

//        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
//        logger.info { "Using $namespace as namespace." }

        val client = DefaultKubernetesClient().inNamespace("default")


        KubernetesDeserializer.registerCustomKind(
            "demo.k8s.io/v1alpha1",
            "Benchmarkexecutions",
            BenchmarkExecution::class.java
        )


        val context = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("demo.k8s.io")
            .withPlural("benchmarkexecutions")
            .build()

        val informerFactory = client.informers()


        val x = informerFactory.sharedIndexInformerForCustomResource(context, BenchmarkExecution::class.java,
            BenchmarkExecutionList::class.java,10 * 60 * 1000.toLong())


        x.addEventHandler(object : ResourceEventHandler<BenchmarkExecution> {
            override fun onAdd(webServer: BenchmarkExecution) {
                println("hello there add")
                println(webServer.name)
            }

            override fun onUpdate(webServer: BenchmarkExecution, newWebServer: BenchmarkExecution) {
                println("hello there update")
            }

            override fun onDelete(webServer: BenchmarkExecution, b: Boolean) {
                println("delted")
                println(webServer.name)
            }
        })

        informerFactory.startAllRegisteredInformers()



        //println(client.apiextensions().v1beta1().customResourceDefinitions().list())

        //exitProcess(0)
    }
}
