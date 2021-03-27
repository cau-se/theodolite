package theodolite.execution

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.BenchmarkExecutionList
import theodolite.benchmark.KubernetesBenchmark
import theodolite.benchmark.KubernetesBenchmarkList


private var DEFAULT_NAMESPACE = "default"
private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteOperator")
object TheodoliteOperator {
    @JvmStatic
    fun main(args: Array<String>) {

        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        logger.info { "Using $namespace as namespace." }


        val client = DefaultKubernetesClient().inNamespace(namespace)

        KubernetesDeserializer.registerCustomKind(
            "theodolite.com/v1alpha1",
            "execution",
            BenchmarkExecution::class.java
        )

        KubernetesDeserializer.registerCustomKind(
            "theodolite.com/v1alpha1",
            "benchmark",
            KubernetesBenchmark::class.java
        )

        val executionContext = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("theodolite.com")
            .withPlural("executions")
            .build()

        val typeContext = CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaced")
            .withGroup("theodolite.com")
            .withPlural("benchmarks")
            .build()

        val informerFactory = client.informers()

        val informerBenchmarkExecution = informerFactory.sharedIndexInformerForCustomResource(
            executionContext, BenchmarkExecution::class.java,
            BenchmarkExecutionList::class.java, 10 * 60 * 1000.toLong()
        )

        val informerBenchmarkType = informerFactory.sharedIndexInformerForCustomResource(
            typeContext, KubernetesBenchmark::class.java,
            KubernetesBenchmarkList::class.java, 10 * 60 * 1000.toLong()
        )
        val controller = TheodoliteController(
            client = client,
            informerBenchmarkExecution = informerBenchmarkExecution,
            informerBenchmarkType = informerBenchmarkType,
            executionContext = executionContext
        )

        controller.create()
        informerFactory.startAllRegisteredInformers()
        controller.run()
    }
}
