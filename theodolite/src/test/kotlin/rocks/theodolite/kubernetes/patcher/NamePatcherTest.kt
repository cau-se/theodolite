package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import registerResource

@QuarkusTest
@WithKubernetesTestServer
internal class NamePatcherTest: AbstractPatcherTest() {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    @Test
    fun testService() {
        val sourceResource = createService()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    @Test
    fun testConfigMap() {
        val sourceResource = createConfigMap()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    @Test
    fun testCrd() {
        val sourceResource = createServiceMonitor()
        val patcher = NamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "new-name")
        patchedResources.forEach {
            assertEquals("new-name", it.metadata.name)
        }
    }

    fun createServiceMonitor(): HasMetadata {
        val serviceMonitorContext = ResourceDefinitionContext.Builder()
                .withGroup("monitoring.coreos.com")
                .withKind("ServiceMonitor")
                .withPlural("servicemonitors")
                .withNamespaced(true)
                .withVersion("v1")
                .build()
        server.registerResource(serviceMonitorContext)

        val serviceMonitorStream = javaClass.getResourceAsStream("/k8s-resource-files/test-service-monitor.yaml")
        return server.client.load(serviceMonitorStream).items().first()
    }
}