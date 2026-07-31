package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.client.CustomResourceList
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.*

@QuarkusTest
class RunnerCoordinatorTest {

    private val server = KubernetesServer(false, false)
    private lateinit var coordinator: RunnerCoordinator
    private val mapper = ObjectMapper()

    private var benchmark = KubernetesBenchmark()
    private var execution = BenchmarkExecution()

    private val benchmarkResourceList = CustomResourceList<BenchmarkCRD>()
    private val executionResourceList = CustomResourceList<ExecutionCRD>()

    @BeforeEach
    fun setUp() {
        server.before()

        // Use the public no-arg constructor and set the client directly (not the CDI-managed instance).
        this.coordinator = RunnerCoordinator()
        this.coordinator.client = server.client

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
            .withPath("/apis/theodolite.rocks/v1beta2/namespaces/test/benchmarks")
            .andReturn(200, benchmarkResourceList)
            .always()

        server
            .expect()
            .get()
            .withPath("/apis/theodolite.rocks/v1beta2/namespaces/test/executions")
            .andReturn(200, executionResourceList)
            .always()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    @DisplayName("Check namespaced property of benchmark client")
    fun testBenchmarkClientNamespaced() {
        coordinator.getBenchmarks()

        assert(
            server
                .lastRequest
                .toString()
                .contains("namespaces")
        )
    }

    @Test
    @DisplayName("Check namespaced property of execution client")
    fun testExecutionClientNamespaced() {
        coordinator.selectNext()

        assert(
            server
                .lastRequest
                .toString()
                .contains("namespaces")
        )
    }

    @Test
    fun getBenchmarksTest() {
        val result = coordinator.getBenchmarks()

        assertEquals(2, result.size)
        assertEquals(
            mapper.writeValueAsString(benchmark),
            mapper.writeValueAsString(result.firstOrNull()?.spec)
        )
    }

    @Test
    fun `selectNext returns the oldest matching execution with a ready benchmark`() {
        val result = coordinator.selectNext()

        assertEquals(
            mapper.writeValueAsString(this.execution),
            mapper.writeValueAsString(result?.first?.spec)
        )
    }

    @Test
    fun `isRunning returns false when no execution is active`() {
        assert(!coordinator.isRunning("any-execution"))
    }

    @Test
    fun `getCoordinator returns a non-null singleton`() {
        val server2 = KubernetesServer(false, false)
        server2.before()
        try {
            val operator = TheodoliteOperator(server2.client)
            val first = operator.getCoordinator()
            assertNotNull(first)
            assertSame(first, operator.getCoordinator())
        } finally {
            server2.after()
        }
    }
}
