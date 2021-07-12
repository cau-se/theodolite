package theodolite.execution.operator

import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import theodolite.k8s.K8sManager
import theodolite.k8s.K8sResourceLoader
import theodolite.model.crd.States
import java.time.Duration

class StateHandlerTest {
    private val testResourcePath = "./src/test/resources/k8s-resource-files/"
    private val server = KubernetesServer(false, true)

    @BeforeEach
    fun setUp() {
        server.before()
        val executionResource = K8sResourceLoader(server.client)
            .loadK8sResource("Execution", testResourcePath + "test-execution.yaml")

        K8sManager(server.client).deploy(executionResource)
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    @DisplayName("check if Statehandler is namespaced")
    fun namespacedTest() {
        val handler = ExecutionStateHandler(client = server.client)
        handler.getExecutionState("example-execution")
        assert(server
            .lastRequest
            .toString()
            .contains("namespaces")
        )
    }

    @Test
    @DisplayName("Test empty execution state")
    fun executionWithoutExecutionStatusTest(){
        val handler = ExecutionStateHandler(client = server.client)
        assertEquals(States.NO_STATE, handler.getExecutionState("example-execution"))
    }

    @Test
    @DisplayName("Test empty duration state")
    fun executionWithoutDurationStatusTest(){
        val handler = ExecutionStateHandler(client = server.client)
        assertEquals("-", handler.getDurationState("example-execution"))
    }

    @Test
    @DisplayName("Test set and get of the execution state")
    fun executionStatusTest() {
        val handler = ExecutionStateHandler(client = server.client)

        assertTrue(handler.setExecutionState("example-execution", States.INTERRUPTED))
        assertEquals(States.INTERRUPTED, handler.getExecutionState("example-execution"))
    }

    @Test
    @DisplayName("Test set and get of the duration state")
    fun durationStatusTest() {
        val handler = ExecutionStateHandler(client = server.client)

        assertTrue(handler.setDurationState("example-execution", Duration.ofMillis(100)))
        assertEquals("0s", handler.getDurationState("example-execution"))
    }
}