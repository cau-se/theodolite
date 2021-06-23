package theodolite.execution.operator

import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import theodolite.k8s.K8sContextFactory
import theodolite.k8s.K8sManager
import theodolite.k8s.K8sResourceLoader
import theodolite.model.crd.States
import java.time.Duration

class StateHandlerTest {
    private val testResourcePath = "./src/test/resources/k8s-resource-files/"
    private val server = KubernetesServer(false,true)
    private val context = K8sContextFactory().create(
        api = "v1",
        scope = "Namespaced",
        group = "theodolite.com",
        plural = "executions"
    )


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
    @DisplayName("Test set and get of the execution state")
    fun executionStatusTest() {
        val handler = ExecutionStateHandler(client = server.client, context = context)

        assertTrue(handler.setExecutionState("example-execution", States.INTERRUPTED))
        assertEquals(States.INTERRUPTED,handler.getExecutionState("example-execution") )
    }

    @Test
    @DisplayName("Test set and get of the duration state")
    fun durationStatusTest() {
        val handler = ExecutionStateHandler(client = server.client, context = context)

        assertTrue(handler.setDurationState("example-execution", Duration.ofMillis(100)))
        assertEquals("0s",handler.getDurationState("example-execution") )
    }
}