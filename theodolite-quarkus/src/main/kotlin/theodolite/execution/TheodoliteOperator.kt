package theodolite.execution

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
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

        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        logger.info { "Using $namespace as namespace." }

        val client = DefaultKubernetesClient().inNamespace("default")


        KubernetesDeserializer.registerCustomKind(
            "demo.k8s.io/v1alpha1",
            "Benchmarkexecutions",
            BenchmarkExecution::class.java
        )

        KubernetesDeserializer.registerCustomKind(
            "demo.k8s.io/v1alpha1",
            "Benchmarktype",
            KubernetesBenchmark::class.java
        )

        val ExececutionContext = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("demo.k8s.io")
            .withPlural("benchmarkexecutions")
            .build()

        val TypeContext = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("demo.k8s.io")
            .withPlural("benchmarktypes")
            .build()

        val informerFactory = client.informers()


        val informerBenchmarkExecution = informerFactory.sharedIndexInformerForCustomResource(ExececutionContext, BenchmarkExecution::class.java,
            BenchmarkExecutionList::class.java,10 * 60 * 1000.toLong())

        val informerBenchmarkType = informerFactory.sharedIndexInformerForCustomResource(TypeContext, KubernetesBenchmark::class.java,
            KubernetesBenchmarkList::class.java,10 * 60 * 1000.toLong())



        val controller = TheodoliteController(client = client,
            informerBenchmarkExecution = informerBenchmarkExecution,
            informerBenchmarkType = informerBenchmarkType)

        controller.create()

        informerFactory.startAllRegisteredInformers()

        controller.run()

        //exitProcess(0)
    }
}
