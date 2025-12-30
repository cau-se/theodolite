package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.EnvVarBuilder
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
internal class EnvVarPatcherTest : AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = EnvVarPatcher("main-container", "TEST_ENV")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            val containers = (it as Deployment).spec.template.spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            val expectedEnvVar = EnvVarBuilder().withName("TEST_ENV").withValue("some-value").build()
            assertTrue(containers[0].env.contains(expectedEnvVar))
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = EnvVarPatcher("main-container", "TEST_ENV")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            val containers = (it as StatefulSet).spec.template.spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            val expectedEnvVar = EnvVarBuilder().withName("TEST_ENV").withValue("some-value").build()
            assertTrue(containers[0].env.contains(expectedEnvVar))
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = EnvVarPatcher("main-container", "TEST_ENV")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            val containers = (it as Pod).spec.containers.filter { it.name == "main-container" }
            assertEquals(1, containers.size)
            val expectedEnvVar = EnvVarBuilder().withName("TEST_ENV").withValue("some-value").build()
            assertTrue(containers[0].env.contains(expectedEnvVar))
        }
    }

    @Test
    fun testInitContainer() {
        val sourceResource = createDeployment()
        val patcher = EnvVarPatcher("init-container", "SOME_NUMERIC_VAR", isInitContainer = true)
        val patchedResources = patcher.patch(listOf(sourceResource), "456")
        patchedResources.forEach {
            val initContainers = (it as Deployment).spec.template.spec.initContainers.filter { it.name == "init-container" }
            assertEquals(1, initContainers.size)
            val expectedEnvVar = EnvVarBuilder().withName("SOME_NUMERIC_VAR").withValue("456").build()
            assertTrue(initContainers[0].env.contains(expectedEnvVar))
        }
    }
}