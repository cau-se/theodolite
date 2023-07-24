package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
@WithKubernetesTestServer
class ResourceLimitPatcherTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    fun applyTest(fileName: String) {
        val cpuValue = "50m"
        val memValue = "3Gi"
        val k8sResource = getDeployment(fileName)

        val defCPU = PatcherDefinition()
        defCPU.resource = "/cpu-memory-deployment.yaml"
        defCPU.type = "ResourceLimitPatcher"
        defCPU.properties = mapOf(
            "limitedResource" to "cpu",
            "container" to "uc-application"
        )

        val defMEM = PatcherDefinition()
        defMEM.resource = "/cpu-memory-deployment.yaml"
        defMEM.type = "ResourceLimitPatcher"
        defMEM.properties = mapOf(
            "limitedResource" to "memory",
            "container" to "uc-application"
        )

        val firstPatched = PatchHandler.patchResource(mutableMapOf(Pair("/cpu-memory-deployment.yaml", listOf(k8sResource))), defCPU, cpuValue)
        val finalPatched = PatchHandler.patchResource(mutableMapOf(Pair("/cpu-memory-deployment.yaml",  firstPatched)), defMEM, memValue)
        assertEquals(1, finalPatched.size)
        (finalPatched[0] as Deployment).spec.template.spec.containers.filter { it.name == defCPU.properties["container"]!! }
            .forEach {
                assertEquals(cpuValue, it.resources.limits["cpu"].toString())
                assertEquals(memValue, it.resources.limits["memory"].toString())
            }
    }

    @Test
    fun testWithExistingCpuAndMemoryDeclarations() {
        // Case 1: In the given YAML declaration memory and cpu are defined
        applyTest("/cpu-memory-deployment.yaml")
    }

    @Test
    fun testOnlyWithExistingCpuDeclarations() {
        // Case 2:  In the given YAML declaration only cpu is defined
        applyTest("/cpu-deployment.yaml")
    }

    @Test
    fun testOnlyWithExistingMemoryDeclarations() {
        //  Case 3:  In the given YAML declaration only memory is defined
        applyTest("/memory-deployment.yaml")
    }

    @Test
    fun testWithoutResourceDeclarations() {
        // Case 4: In the given YAML declaration neither `Resource Request` nor `Request Limit` is defined
        applyTest("/no-resources-deployment.yaml")
    }

    @Test
    fun testWithNoFactorSet() {
        val initialDeployment = getDeployment("/cpu-memory-deployment.yaml")
        val patchedDeployments = ResourceLimitPatcher(
            "uc-application",
            "memory"
        ).patch(listOf(initialDeployment), "1Gi")
        assertEquals(1, patchedDeployments.size)
        val patchedDeployment = patchedDeployments[0] as Deployment

        val containers = patchedDeployment.spec.template.spec.containers.filter { it.name == "uc-application" }
        assertEquals(1, containers.size)
        containers.forEach {
                assertEquals("1Gi", it.resources.limits["memory"].toString())
            }
    }

    @Test
    fun testWithFormatSet() {
        val initialDeployment = getDeployment("/cpu-memory-deployment.yaml")
        val patchedDeployments = ResourceLimitPatcher(
            "uc-application",
            "memory",
            format = "GBi"
        ).patch(listOf(initialDeployment), "2")
        assertEquals(1, patchedDeployments.size)
        val patchedDeployment = patchedDeployments[0] as Deployment

        val containers = patchedDeployment.spec.template.spec.containers.filter { it.name == "uc-application" }
        assertEquals(1, containers.size)
        containers.forEach {
                assertEquals("2GBi", it.resources.limits["memory"].toString())
            }
    }

    @Test
    fun testWithFactorSet() {
        val initialDeployment = getDeployment("/cpu-memory-deployment.yaml")
        val patchedDeployments = ResourceLimitPatcher(
            "uc-application",
            "memory",
            factor = 4000
        ).patch(listOf(initialDeployment), "2")
        assertEquals(1, patchedDeployments.size)
        val patchedDeployment = patchedDeployments[0] as Deployment

        val containers = patchedDeployment.spec.template.spec.containers.filter { it.name == "uc-application" }
        assertEquals(1, containers.size)
        containers.forEach {
            assertEquals("8000", it.resources.limits["memory"].toString())
        }
    }

    @Test
    fun testWithFactorAndFormatSet() {
        val initialDeployment = getDeployment("/cpu-memory-deployment.yaml")
        val patchedDeployments = ResourceLimitPatcher(
            "uc-application",
            "memory",
            format = "GBi",
            factor = 4,
        ).patch(listOf(initialDeployment), "2")
        assertEquals(1, patchedDeployments.size)
        val patchedDeployment = patchedDeployments[0] as Deployment

        val containers = patchedDeployment.spec.template.spec.containers.filter { it.name == "uc-application" }
        assertEquals(1, containers.size)
        containers.forEach {
                assertEquals("8GBi", it.resources.limits["memory"].toString())
            }
    }

    private fun getDeployment(fileName: String): Deployment {
        return server.client.apps().deployments().load(javaClass.getResourceAsStream(fileName)).get()
    }
}
