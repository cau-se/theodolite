package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import registerResource

@QuarkusTest
@WithKubernetesTestServer
internal class GenericResourcePatcherTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @Test
    fun testPatchString() {
        val sourceResource = listOf(createServiceMonitor())
        val patcher = GenericResourcePatcher(listOf("spec", "endpoints", 0, "interval"))
        val patchedResource = patcher.patch(sourceResource, "20s")
        patchedResource.forEach {
            assertEquals(
                "20s",
                ((((it as GenericKubernetesResource)
                    .additionalProperties["spec"] as Map<String, Any>)
                        ["endpoints"] as List<Any>)
                        [0] as Map<String, Any>)
                        ["interval"])
        }
    }

    @Test
    fun testPatchBoolean() {
        val sourceResource = listOf(createServiceMonitor())
        val patcher = GenericResourcePatcher(listOf("spec", "endpoints", 0, "honorTimestamps"), GenericResourcePatcher.Type.BOOLEAN)
        val patchedResource = patcher.patch(sourceResource, "true")
        patchedResource.forEach {
            assertEquals(
                true,
                ((((it as GenericKubernetesResource)
                    .additionalProperties["spec"] as Map<String, Any>)
                        ["endpoints"] as List<Any>)
                        [0] as Map<String, Any>)
                        ["honorTimestamps"])
        }
    }

    @Test
    fun testPatchInteger() {
        val sourceResource = listOf(createServiceMonitor())
        val patcher = GenericResourcePatcher(listOf("spec", "labelLimit"), GenericResourcePatcher.Type.INTEGER)
        val patchedResource = patcher.patch(sourceResource, "11")
        patchedResource.forEach {
            assertEquals(
                11,
                ((it as GenericKubernetesResource)
                    .additionalProperties["spec"] as Map<String, Any>)
                        ["labelLimit"])
        }
    }

    @Test
    fun testPatchNumber() {
        val sourceResource = listOf(createServiceMonitor())
        val patcher = GenericResourcePatcher(listOf("spec", "myMadeUpProp"), GenericResourcePatcher.Type.NUMBER)
        val patchedResource = patcher.patch(sourceResource, "11.2")
        patchedResource.forEach {
            assertEquals(
                11.2,
                ((it as GenericKubernetesResource)
                    .additionalProperties["spec"] as Map<String, Any>)
                        ["labelLimit"])
        }
    }

    @Test
    fun testPatchNumberWithoutDecimals() {
        val sourceResource = listOf(createServiceMonitor())
        val patcher = GenericResourcePatcher(listOf("spec", "myMadeUpProp"), GenericResourcePatcher.Type.NUMBER)
        val patchedResource = patcher.patch(sourceResource, "11")
        patchedResource.forEach {
            assertEquals(
                11.0,
                ((it as GenericKubernetesResource)
                    .additionalProperties["spec"] as Map<String, Any>)
                        ["labelLimit"])
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
        return server.client.load(serviceMonitorStream).get()[0]
    }
}
