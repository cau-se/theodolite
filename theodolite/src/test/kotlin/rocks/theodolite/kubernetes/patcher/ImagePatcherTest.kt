package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ImagePatcherTest: AbstractPatcherTest(){

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = ImagePatcher("main-container")
        val patchedResources = patcher.patch(listOf(sourceResource), "some.registry.io/some-repo/image:latest")
        patchedResources.forEach { resource ->
            val containers = (resource as Deployment).spec.template.spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            assertEquals("some.registry.io/some-repo/image:latest", containers[0].image)
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = ImagePatcher("main-container")
        val patchedResources = patcher.patch(listOf(sourceResource), "some.registry.io/some-repo/image:latest")
        patchedResources.forEach { resource ->
            val containers = (resource as StatefulSet).spec.template.spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            assertEquals("some.registry.io/some-repo/image:latest", containers[0].image)
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = ImagePatcher("main-container")
        val patchedResources = patcher.patch(listOf(sourceResource), "some.registry.io/some-repo/image:latest")
        patchedResources.forEach { resource ->
            val containers = (resource as Pod).spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            assertEquals("some.registry.io/some-repo/image:latest", containers[0].image)
        }
    }

    @Test
    fun testPatchNonExistingContainer() {
        val sourceResource = createDeployment()
        val patcher = ImagePatcher("other-container")
        val patchedResources = patcher.patch(listOf(sourceResource), "some.registry.io/some-repo/image:latest")
        patchedResources.forEach { resource ->
            val containers = (resource as Deployment).spec.template.spec.containers
            assertEquals(1, containers.size)
            assertEquals("main-container", containers[0].name)
            assertEquals("test-image", containers[0].image)
        }
    }
}