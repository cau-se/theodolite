package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class VolumesConfigMapPatcherTest: AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = VolumesConfigMapPatcher("test-configmap")
        val patchedResources = patcher.patch(listOf(sourceResource), "patchedVolumeName")
        patchedResources.forEach {
            assertEquals(
                    "patchedVolumeName",
                    (it as Deployment).spec.template.spec.volumes[0].configMap.name)
        }
    }
}