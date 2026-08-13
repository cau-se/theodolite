package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
internal class NodeSelectorPatcherTest: AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = NodeSelectorPatcher("node-label-name")
        val patchedResources = patcher.patch(listOf(sourceResource), "nodeLabelValue")
        patchedResources.forEach {
            assertTrue((it as Deployment).spec.template.spec.nodeSelector.containsKey("node-label-name"))
            assertEquals("nodeLabelValue", it.spec.template.spec.nodeSelector["node-label-name"])
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = NodeSelectorPatcher("node-label-name")
        val patchedResources = patcher.patch(listOf(sourceResource), "nodeLabelValue")
        patchedResources.forEach {
            assertTrue((it as StatefulSet).spec.template.spec.nodeSelector.containsKey("node-label-name"))
            assertEquals("nodeLabelValue", it.spec.template.spec.nodeSelector["node-label-name"])
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = NodeSelectorPatcher("node-label-name")
        val patchedResources = patcher.patch(listOf(sourceResource), "nodeLabelValue")
        patchedResources.forEach {
            assertTrue((it as Pod).spec.nodeSelector.containsKey("node-label-name"))
            assertEquals("nodeLabelValue", it.spec.nodeSelector["node-label-name"])
        }
    }

}