package theodolite.execution.operator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.fabric8.kubernetes.client.CustomResourceList
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import theodolite.benchmark.KubernetesBenchmark
import theodolite.k8s.K8sContextFactory
import theodolite.model.crd.*
import theodolite.util.KafkaConfig

private const val DEFAULT_NAMESPACE = "default"
private const val SCOPE = "Namespaced"
private const val EXECUTION_SINGULAR = "execution"
private const val EXECUTION_PLURAL = "executions"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val BENCHMARK_PLURAL = "benchmarks"
private const val API_VERSION = "v1"
private const val RESYNC_PERIOD = 10 * 60 * 1000.toLong()
private const val GROUP = "theodolite.com"

@QuarkusTest
class ControllerTest {
    private final val server = KubernetesServer(false, false)
    private final val testResourcePath = "./src/test/resources/k8s-resource-files/"
    lateinit var controller: TheodoliteController
    lateinit var benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, DoneableBenchmark, Resource<BenchmarkCRD, DoneableBenchmark>>
    private val gson: Gson = GsonBuilder().enableComplexMapKeySerialization().create()

    private val kafkaConfig = KafkaConfig()
    private val benchmark = KubernetesBenchmark()


    @BeforeEach
    fun setUp() {
        server.before()

        KubernetesDeserializer.registerCustomKind(
            "$GROUP/$API_VERSION",
            EXECUTION_SINGULAR,
            ExecutionCRD::class.java
        )

        KubernetesDeserializer.registerCustomKind(
            "$GROUP/$API_VERSION",
            BENCHMARK_SINGULAR,
            BenchmarkCRD::class.java
        )

        val contextFactory = K8sContextFactory()
        val executionContext = contextFactory.create(API_VERSION, SCOPE, GROUP, EXECUTION_PLURAL)
        val benchmarkContext = contextFactory.create(API_VERSION, SCOPE, GROUP, BENCHMARK_PLURAL)

        val executionCRDClient: MixedOperation<
                ExecutionCRD,
                BenchmarkExecutionList,
                DoneableExecution,
                Resource<ExecutionCRD, DoneableExecution>> = server.client.customResources(
            executionContext,
            ExecutionCRD::class.java,
            BenchmarkExecutionList::class.java,
            DoneableExecution::class.java
        )

        this.benchmarkCRDClient = server.client.customResources(
            benchmarkContext,
            BenchmarkCRD::class.java,
            KubernetesBenchmarkList::class.java,
            DoneableBenchmark::class.java
        )

        val executionStateHandler = ExecutionStateHandler(
            context = executionContext,
            client = server.client
        )

        val appResource = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"
        this.controller =
            TheodoliteController(
                namespace = server.client.namespace,
                path = appResource,
                benchmarkCRDClient = benchmarkCRDClient,
                executionCRDClient = executionCRDClient,
                executionStateHandler = executionStateHandler
            )


        // create benchmarks

        kafkaConfig.bootstrapServer = ""
        kafkaConfig.topics = emptyList()

        benchmark.name = "Test-Benchmark"
        benchmark.appResource = emptyList()
        benchmark.loadGenResource = emptyList()
        benchmark.resourceTypes = emptyList()
        benchmark.loadTypes = emptyList()
        benchmark.kafkaConfig = kafkaConfig

    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    fun test() {
        val crd = BenchmarkCRD(benchmark)
        crd.spec = benchmark
        crd.metadata.name = "Test-Benchmark"
        crd.kind = "Benchmark"
        crd.apiVersion = "v1"

        val list = CustomResourceList<BenchmarkCRD>()
        list.items = listOf(crd)

        server.expect().get().withPath("/apis/theodolite.com/v1/namespaces/test/benchmarks").andReturn(200, list).always()

        val method = controller.javaClass.getDeclaredMethod("getBenchmarks")
        method.isAccessible = true
        benchmark.name = crd.metadata.name
        val result = method.invoke(controller) as List<KubernetesBenchmark>
        assertEquals(gson.toJson(result[0]), gson.toJson(benchmark))
    }
}