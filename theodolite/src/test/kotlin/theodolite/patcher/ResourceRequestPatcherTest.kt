package theodolite.patcher

import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import io.smallrye.common.constraint.Assert.assertTrue
import org.junit.jupiter.api.Test
import theodolite.util.PatcherDefinition

/**
 * Resource patcher test
 *
 * This class tested 4 scenarios for the ResourceLimitPatcher and the ResourceRequestPatcher.
 * The different test cases specifies four possible situations:
 * Case 1:  In the given YAML declaration memory and cpu are defined
 * Case 2:  In the given YAML declaration only cpu is defined
 * Case 3:  In the given YAML declaration only memory is defined
 * Case 4:  In the given YAML declaration neither `Resource Request` nor `Request Limit` is defined
 */
@QuarkusTest
@WithKubernetesTestServer
class ResourceRequestPatcherTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    val patcherFactory = PatcherFactory()

    fun applyTest(fileName: String) {
        val cpuValue = "50m"
        val memValue = "3Gi"
        val k8sResource = server.client.apps().deployments().load(javaClass.getResourceAsStream(fileName)).get()

        val defCPU = PatcherDefinition()
        defCPU.resource = "/cpu-memory-deployment.yaml"
        defCPU.type = "ResourceRequestPatcher"
        defCPU.properties = mapOf(
            "requestedResource" to "cpu",
            "container" to "application"
        )

        val defMEM = PatcherDefinition()
        defMEM.resource = "/cpu-memory-deployment.yaml"
        defMEM.type = "ResourceRequestPatcher"
        defMEM.properties = mapOf(
            "requestedResource" to "memory",
            "container" to "application"
        )

        patcherFactory.createPatcher(
            patcherDefinition = defCPU,
            k8sResources = listOf(Pair("/cpu-memory-deployment.yaml", k8sResource))
        ).patch(value = cpuValue)
        patcherFactory.createPatcher(
            patcherDefinition = defMEM,
            k8sResources = listOf(Pair("/cpu-memory-deployment.yaml", k8sResource))
        ).patch(value = memValue)

        k8sResource.spec.template.spec.containers.filter { it.name == defCPU.properties["container"]!! }
            .forEach {
                assertTrue(it.resources.requests["cpu"].toString() == cpuValue)
                assertTrue(it.resources.requests["memory"].toString() == memValue)
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
}
