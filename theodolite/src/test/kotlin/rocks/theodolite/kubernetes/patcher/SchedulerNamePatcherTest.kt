package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
internal class SchedulerNamePatcherTest : AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = SchedulerNamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "testScheduler")
        patchedResources.forEach {
            assertEquals("testScheduler", (it as Deployment).spec.template.spec.schedulerName)
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = SchedulerNamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "testScheduler")
        patchedResources.forEach {
            assertEquals("testScheduler", (it as StatefulSet).spec.template.spec.schedulerName)
        }
    }

    @Test
    fun testPod() {
        val sourceResource = createPod()
        val patcher = SchedulerNamePatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "testScheduler")
        patchedResources.forEach {
            assertEquals("testScheduler", (it as Pod).spec.schedulerName)
        }
    }
}