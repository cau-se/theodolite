package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import registerResource

@QuarkusTest
@WithKubernetesTestServer
internal class LabelPatcherTest: AbstractPatcherTest() {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue(it.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue(it.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue(it.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
        }
    }

    @Test
    fun testService() {
        val sourceResource = createService()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as Service).metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
        }
    }

    @Test
    fun testConfigMap() {
        val sourceResource = createConfigMap()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue(it.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
        }
    }

    @Test
    fun testCrd() {
        val sourceResource = createServiceMonitor()
        val patcher = LabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue(it.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.metadata.labels["some-label"])
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