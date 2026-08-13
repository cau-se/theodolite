package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class TemplateLabelPatcherTest: AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = TemplateLabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as Deployment).spec.template.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.spec.template.metadata.labels["some-label"])
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = TemplateLabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as StatefulSet).spec.template.metadata.labels.containsKey("some-label"))
            assertEquals("some-value", it.spec.template.metadata.labels["some-label"])
        }
    }


}