package theodolite.execution.operator

import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.k8s.K8sManager
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.operator.ExecutionStateHandler

@QuarkusTest
@WithKubernetesTestServer
class StateHandlerTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @BeforeEach
    fun setUp() {
        server.before()
        val executionStream = javaClass.getResourceAsStream("/k8s-resource-files/test-execution.yaml")
        val executionResource = server.client.resources(ExecutionCRD::class.java).load(executionStream).get()

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
        assert(
            server
                .lastRequest
                .toString()
                .contains("namespaces")
        )
    }

    @Test
    @DisplayName("Test empty execution state")
    fun executionWithoutExecutionStatusTest() {
        val handler = ExecutionStateHandler(client = server.client)
        assertEquals(ExecutionState.NO_STATE, handler.getExecutionState("example-execution"))
    }

    @Test
    @DisplayName("Test set and get of the execution state")
    fun executionStatusTest() {
        val handler = ExecutionStateHandler(client = server.client)

        assertTrue(handler.setExecutionState("example-execution", ExecutionState.INTERRUPTED))
        assertEquals(ExecutionState.INTERRUPTED, handler.getExecutionState("example-execution"))
    }

}