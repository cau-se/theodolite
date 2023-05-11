package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
internal class NumNestedGroupsLoadGeneratorReplicaPatcherTest : AbstractPatcherTest(){

    @Test
    fun testFullyUtilizedGeneratorDeployment() {
        testInputVsExpectedForDeployment(262144,10, 4)
    }

    @Test
    fun testNonFullyUtilizedGeneratorsDeployment() {
        testInputVsExpectedForDeployment(250000, 10, 5)
    }

    @Test
    fun testFullyUtilizedGeneratorStatefulSet() {
        testInputVsExpectedForSts(262144, 10, 4)
    }

    @Test
    fun testNonFullyUtilizedGeneratorsStatefulSet() {
        testInputVsExpectedForSts(250000,10, 5)
    }

    private fun testInputVsExpectedForDeployment(loadGenMaxRecords: Int, inputLoad: Int, expectedReplicas: Int) {
        val sourceResource = createDeployment()
        val patcher = NumNestedGroupsLoadGeneratorReplicaPatcher(4, loadGenMaxRecords)
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            assertEquals(expectedReplicas, (resource as Deployment).spec.replicas)
        }
    }

    private fun testInputVsExpectedForSts(loadGenMaxRecords: Int, inputLoad: Int, expectedReplicas: Int) {
        val sourceResource = createStatefulSet()
        val patcher = NumNestedGroupsLoadGeneratorReplicaPatcher(4, loadGenMaxRecords)
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            assertEquals(expectedReplicas, (resource as StatefulSet).spec.replicas)
        }
    }

}