package theodolite.execution.operator

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.BenchmarkExecutionList
import theodolite.benchmark.KubernetesBenchmark
import theodolite.benchmark.KubernetesBenchmarkList


private const val DEFAULT_NAMESPACE = "default"
private const val SCOPE = "Namespaced"
private const val EXECUTION_SINGULAR = "execution"
private const val EXECUTION_PLURAL = "executions"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val BENCHMARK_PLURAL = "benchmarks"
private const val API_VERSION = "v1alpha1"
private const val RESYNC_PERIOD = 10 * 60 * 1000.toLong()
private const val GROUP = "theodolite.com"
private val logger = KotlinLogging.logger {}

class TheodoliteOperator {
    private val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE

    fun start() {
        logger.info { "Using $namespace as namespace." }
        val client = DefaultKubernetesClient().inNamespace(namespace)

        KubernetesDeserializer.registerCustomKind(
            "$GROUP/$API_VERSION",
            EXECUTION_SINGULAR,
            BenchmarkExecution::class.java
        )

        KubernetesDeserializer.registerCustomKind(
            "$GROUP/$API_VERSION",
            BENCHMARK_SINGULAR,
            KubernetesBenchmark::class.java
        )

        val contextFactory = K8sContextFactory()
        val executionContext = contextFactory.create(API_VERSION, SCOPE, GROUP, EXECUTION_PLURAL)
        val benchmarkContext = contextFactory.create(API_VERSION, SCOPE, GROUP, BENCHMARK_PLURAL)

        val appResource = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"
        val controller = TheodoliteController(client = client, executionContext = executionContext, path = appResource)

        val informerFactory = client.informers()
        val informerExecution = informerFactory.sharedIndexInformerForCustomResource(
            executionContext, BenchmarkExecution::class.java,
            BenchmarkExecutionList::class.java, RESYNC_PERIOD
        )
        val informerBenchmark = informerFactory.sharedIndexInformerForCustomResource(
            benchmarkContext, KubernetesBenchmark::class.java,
            KubernetesBenchmarkList::class.java, RESYNC_PERIOD
        )

        informerExecution.addEventHandler(ExecutionHandler(controller))
        informerBenchmark.addEventHandler(BenchmarkEventHandler(controller))
        informerFactory.startAllRegisteredInformers()

        controller.run()
    }
}
