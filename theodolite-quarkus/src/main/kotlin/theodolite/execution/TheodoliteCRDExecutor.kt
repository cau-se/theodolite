package theodolite.execution

import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinitionBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.*
import theodolite.util.YamlParser
import kotlin.system.exitProcess

private var DEFAULT_NAMESPACE = "default"
private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteCRDExecutor")
object TheodoliteCRDExecutor {
    @JvmStatic
    fun main(args: Array<String>) {

//        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
//        logger.info { "Using $namespace as namespace." }

        val client = DefaultKubernetesClient().inNamespace("default")


//        val customResourceDefinition = CustomResourceDefinitionBuilder()
//            .withNewMetadata().withName("benchmarkExecutions.demo.k8s.io").endMetadata()
//            .withNewSpec()
//            .withGroup("demo.k8s.io")
//            .withVersion("v1alpha1")
//            .withNewNames().withKind("BenchmarkExecution").withPlural("benchmarkExecutions").endNames()
//            .withScope("Namespaced")
//            .endSpec()
//            .build()

        val context = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("demo.k8s.io")
            .withPlural("benchmarkexecutions")
            .build()

        val informerFactory = client.informers()


        val x = informerFactory.sharedIndexInformerForCustomResource(context, TestResource::class.java,
            TestResourceList::class.java,10 * 60 * 1000.toLong())


        x.addEventHandler(object : ResourceEventHandler<TestResource> {
            override fun onAdd(webServer: TestResource) {
                println("hello there")
            }

            override fun onUpdate(webServer: TestResource, newWebServer: TestResource) {
                println("hello there")
            }

            override fun onDelete(webServer: TestResource, b: Boolean) {
                println("delted")
            }
        })

        informerFactory.startAllRegisteredInformers()




        //println(client.apiextensions().v1beta1().customResourceDefinitions().list())

        //exitProcess(0)
    }
}
