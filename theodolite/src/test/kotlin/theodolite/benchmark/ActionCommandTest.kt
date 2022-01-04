package theodolite.benchmark

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.PodListBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.fabric8.kubernetes.client.utils.Utils
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import theodolite.execution.operator.TheodoliteController
import theodolite.execution.operator.TheodoliteOperator
import theodolite.util.ActionCommandFailedException

@QuarkusTest
class ActionCommandTest {
    private val server = KubernetesServer(false, false)
    lateinit var controller: TheodoliteController

    @BeforeEach
    fun setUp() {
        server.before()
        val operator = TheodoliteOperator()
        this.controller = operator.getController(
            client = server.client,
            executionStateHandler = operator.getExecutionStateHandler(client = server.client),
            benchmarkStateChecker = operator.getBenchmarkStateChecker(client = server.client)
        )

        val pod: Pod = PodBuilder().withNewMetadata()
            .withName("pod1")
            .withResourceVersion("1")
            .withLabels<String, String>(mapOf("app" to "pod"))
            .withNamespace("test").and()
            .build()

        val ready: Pod = createReadyFrom(pod, "True")

        val podList = PodListBuilder().build()
        podList.items.add(0, ready)

        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods?labelSelector=${Utils.toUrlEncoded("app=pod")}")
            .andReturn(200, podList)
            .always()

        server
            .expect()
            .get()
            .withPath("/api/v1/namespaces/test/pods/pod1")
            .andReturn(200, ready)
            .always()

        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods/pod1/exec?command=ls&stdout=true&stderr=true")
            .andUpgradeToWebSocket()
            .open(ErrorChannelMessage("{\"metadata\":{},\"status\":\"Success\"}\n"))
            .done()
            .always()

        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods/pod1/exec?command=error-command&stdout=true&stderr=true")
            .andUpgradeToWebSocket()
            .open(ErrorChannelMessage("{\"metadata\":{},\"status\":\"failed\", \"details\":{}}\n"))
            .done()
            .always()
    }

    /**
     * Copied from fabric8 Kubernetes Client repository
     *
     * @param pod
     * @param status
     * @return
     */
    fun createReadyFrom(pod: Pod, status: String): Pod {
        return PodBuilder(pod)
            .withNewStatus()
            .addNewCondition()
            .withType("Ready")
            .withStatus(status)
            .endCondition()
            .endStatus()
            .build()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    fun testGetPodName() {
        assertEquals("pod1", ActionCommand(client = server.client).getPodName(mutableMapOf("app" to "pod")))
    }

    @Test
    fun testActionSuccess() {
        val action = Action()
        action.selector = ActionSelector()
        action.selector.pod = PodSelector()
        action.selector.pod.matchLabels = mutableMapOf("app" to "pod")
        action.exec = Command()
        action.exec.command = arrayOf("ls")
        action.exec.timeoutSeconds = 10L

        action.exec(server.client)
        assertEquals(
            "/api/v1/namespaces/test/pods/pod1/exec?command=ls&stdout=true&stderr=true",
            server.lastRequest.path)
    }

    @Test
    fun testActionFailed() {
        val action = Action()
        action.selector = ActionSelector()
        action.selector.pod = PodSelector()
        action.selector.pod.matchLabels = mutableMapOf("app" to "pod")
        action.exec = Command()
        action.exec.command = arrayOf("error-command")
        action.exec.timeoutSeconds = 10L

        assertThrows<ActionCommandFailedException> { run { action.exec(server.client) } }
    }
}
