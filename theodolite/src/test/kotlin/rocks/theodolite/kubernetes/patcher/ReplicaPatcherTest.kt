package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ReplicaPatcherTest: AbstractPatcherTest() {

    @Test
    fun testDeployment() {
        val sourceResource = createDeployment()
        val patcher = ReplicaPatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "5")
        patchedResources.forEach { resource ->
            assertEquals(5, (resource as Deployment).spec.replicas)
        }
    }

    @Test
    fun testStatefulSet() {
        val sourceResource = createStatefulSet()
        val patcher = ReplicaPatcher()
        val patchedResources = patcher.patch(listOf(sourceResource), "5")
        patchedResources.forEach { resource ->
            assertEquals(5, (resource as StatefulSet).spec.replicas)
        }
    }
}