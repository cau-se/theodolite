package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.client.CustomResourceList
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.*


@QuarkusTest
class ControllerTest {
    private final val server = KubernetesServer(false, false)
    lateinit var controller: TheodoliteController
    private val mapper = ObjectMapper()

    private var benchmark = KubernetesBenchmark()
    private var execution = BenchmarkExecution()

    private val benchmarkResourceList = CustomResourceList<BenchmarkCRD>()
    private val executionResourceList = CustomResourceList<ExecutionCRD>()


    @BeforeEach
    fun setUp() {
        server.before()
        val operator = TheodoliteOperator(server.client)
        this.controller = operator.getController(
            executionStateHandler = operator.getExecutionStateHandler(),
            benchmarkStateChecker = operator.getBenchmarkStateChecker()
        )

        // benchmark
        val benchmark1 = BenchmarkCRDummy(name = "Test-Benchmark")
        benchmark1.getCR().status.resourceSetsState = BenchmarkState.READY
        val benchmark2 = BenchmarkCRDummy(name = "Test-Benchmark-123")
        benchmarkResourceList.items = listOf(benchmark1.getCR(), benchmark2.getCR())

        // execution
        val execution1 = ExecutionCRDummy(name = "matching-execution", benchmark = "Test-Benchmark")
        val execution2 = ExecutionCRDummy(name = "non-matching-execution", benchmark = "Test-Benchmark-456")
        val execution3 = ExecutionCRDummy(name = "second-matching-execution", benchmark = "Test-Benchmark")
        executionResourceList.items = listOf(execution1.getCR(), execution2.getCR(), execution3.getCR())

        this.benchmark = benchmark1.getCR().spec
        this.execution = execution1.getCR().spec

        server
            .expect()
            .get()
            .withPath("/apis/theodolite.rocks/v1beta1/namespaces/test/benchmarks")
            .andReturn(200, benchmarkResourceList)
            .always()

        server
            .expect()
            .get()
            .withPath("/apis/theodolite.rocks/v1beta1/namespaces/test/executions")
            .andReturn(200, executionResourceList)
            .always()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    @DisplayName("Check namespaced property of benchmarkCRDClient")
    fun testBenchmarkClientNamespaced() {
        val method = controller
            .javaClass
            .getDeclaredMethod("getBenchmarks")
        method.isAccessible = true
        method.invoke(controller)

        assert(
            server
                .lastRequest
                .toString()
                .contains("namespaces")
        )
    }

    @Test
    @DisplayName("Check namespaced property of executionCRDClient")
    fun testExecutionClientNamespaced() {
        val method = controller
            .javaClass
            .getDeclaredMethod("getNextExecution")
        method.isAccessible = true
        method.invoke(controller)

        assert(
            server
                .lastRequest
                .toString()
                .contains("namespaces")
        )
    }

    @Test
    fun getBenchmarksTest() {
        val method = controller
            .javaClass
            .getDeclaredMethod("getBenchmarks")
        method.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(controller) as List<BenchmarkCRD>

        assertEquals(2, result.size)
        assertEquals(
            mapper.writeValueAsString(benchmark),
            mapper.writeValueAsString(result.firstOrNull()?.spec)
        )
    }

    @Test
    fun getNextExecution() {
        val method = controller
            .javaClass
            .getDeclaredMethod("getNextExecution")
        method.isAccessible = true

        val result = method.invoke(controller) as BenchmarkExecution?

        assertEquals(
            mapper.writeValueAsString(this.execution),
            mapper.writeValueAsString(result)
        )
    }
}