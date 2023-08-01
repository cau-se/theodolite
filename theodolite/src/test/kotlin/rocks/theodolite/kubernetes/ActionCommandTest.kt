package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.http.*
import io.fabric8.kubernetes.client.utils.Serialization
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture


@QuarkusTest
class ActionCommandTest {

    private lateinit var factory: TestStandardHttpClientFactory
    private lateinit var client: KubernetesClient
    private lateinit var httpClient: TestStandardHttpClient

    @BeforeEach
    fun setUp() {
        factory = TestStandardHttpClientFactory()
        client = KubernetesClientBuilder()
                .withHttpClientFactory(factory)
                .withConfig(ConfigBuilder()
                        .withNamespace("test")
                        .build())
                .build()
        httpClient = factory.instances.iterator().next()

        val podReadyList = PodListBuilder()
                .withNewMetadata().endMetadata()
                .addNewItem()
                .withNewMetadata()
                .withName("single-container")
                .withLabels<String, String>(mapOf("app" to "single-container"))
                .endMetadata()
                .withNewSpec().addNewContainer().withName("single-container").endContainer().endSpec()
                .withNewStatus().addNewCondition().withType("Ready").withStatus("True").endCondition().endStatus()
                .endItem()
                .build()
        httpClient.expect("/api/v1/namespaces/test/pods", 200, client.kubernetesSerialization.asJson(podReadyList))
        httpClient.expect("/api/v1/namespaces/test/pods/single-container", 200, client.kubernetesSerialization.asJson(podReadyList.items[0]))
        // "/api/v1/namespaces/test/pods" will be called twice
        httpClient.expect("/api/v1/namespaces/test/pods", 200, client.kubernetesSerialization.asJson(podReadyList))
    }


    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testGetPodName() {
        assertEquals("single-container", ActionCommand(client = client).awaitPodName(mapOf("app" to "single-container"), 1))
    }

    @Test
    fun testActionSuccess() {
        val success = StatusBuilder()
                .withStatus("Success")
                .build()
        httpClient.wsExpect("/api/v1/namespaces/test/pods/single-container/exec", buildWsFutureProvider(success))

        val action = Action().apply {
            execCommand = ExecCommand().apply {
                selector = ExecActionSelector().apply {
                    pod = PodSelector().apply {
                        matchLabels = mapOf("app" to "single-container")
                    }
                }
                command = arrayOf("ls")
                timeoutSeconds = 10L
            }
        }
        action.exec(this.client)

        val calledUri = httpClient.recordedBuildWebSocketDirects[0].standardWebSocketBuilder.asHttpRequest().uri()
        assertTrue(calledUri.toString().contains("/api/v1/namespaces/test/pods/single-container/exec"))
        assertTrue(calledUri.toString().contains("command=ls"))
    }

    @Test
    fun testActionFailed() {
        val failed = StatusBuilder()
                .withStatus("failed")
                .withNewDetails()
                .endDetails()
                .build()
        httpClient.wsExpect("/api/v1/namespaces/test/pods/single-container/exec", buildWsFutureProvider(failed))

        val action = Action().apply {
            execCommand = ExecCommand().apply {
                selector = ExecActionSelector().apply {
                    pod = PodSelector().apply {
                        matchLabels = mapOf("app" to "pod")
                    }
                }
                command = arrayOf("exit", "1")
                timeoutSeconds = 10L
            }
        }

        assertThrows<ActionCommandFailedException> { action.exec(this.client) }

        val calledUri = httpClient.recordedBuildWebSocketDirects[0].standardWebSocketBuilder.asHttpRequest().uri()
        assertTrue(calledUri.toString().contains("/api/v1/namespaces/test/pods/single-container/exec"))
        assertTrue(calledUri.toString().contains("command=exit"))
        assertTrue(calledUri.toString().contains("command=1"))
    }

    private fun buildBodyBytes(prefix: Byte, body: String): ByteBuffer {
        val original = body.toByteArray(StandardCharsets.UTF_8)
        return ByteBuffer.allocate(original.size + 1).apply {
            put(prefix)
            put(original)
        }
    }

    private fun buildWsFutureProvider(status: Status): TestStandardHttpClient.WsFutureProvider {
        val webSocket = mock<WebSocket> {
            on { send(ArgumentMatchers.any()) } doReturn(true)
        }
        return TestStandardHttpClient.WsFutureProvider { _, l ->
            l.onOpen(webSocket)
            val message = buildBodyBytes(3, Serialization.asJson(status))
            l.onMessage(webSocket, message) // exit
            CompletableFuture.completedFuture(WebSocketResponse(WebSocketUpgradeResponse(null), webSocket))
        }
    }
}
