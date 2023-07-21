package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class MatchLabelPatcherTest: AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = MatchLabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as Deployment).spec.selector.matchLabels.containsKey("some-label"))
            assertEquals("some-value", it.spec.selector.matchLabels["some-label"])
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = MatchLabelPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as StatefulSet).spec.selector.matchLabels.containsKey("some-label"))
            assertEquals("some-value", it.spec.selector.matchLabels["some-label"])
        }
    }

}