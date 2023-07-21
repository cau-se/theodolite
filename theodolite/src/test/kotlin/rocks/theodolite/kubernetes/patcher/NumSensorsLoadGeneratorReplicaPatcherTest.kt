package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
internal class NumSensorsLoadGeneratorReplicaPatcherTest: AbstractPatcherTest() {

    @Test
    fun testFullyUtilizedGeneratorDeployment() {
        testInputVsExpectedForDeployment(50000, 5)
    }

    @Test
    fun testNonFullyUtilizedGeneratorsDeployment() {
        testInputVsExpectedForDeployment(45000, 5)
    }

    @Test
    fun testFullyUtilizedGeneratorStatefulSet() {
        testInputVsExpectedForSts(50000, 5)
    }

    @Test
    fun testNonFullyUtilizedGeneratorsStatefulSet() {
        testInputVsExpectedForSts(45000, 5)
    }

    private fun testInputVsExpectedForDeployment(inputLoad: Int, expectedReplicas: Int) {
        val sourceResource = createDeployment()
        val patcher = NumSensorsLoadGeneratorReplicaPatcher(10000)
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            assertEquals(expectedReplicas, (resource as Deployment).spec.replicas)
        }
    }

    private fun testInputVsExpectedForSts(inputLoad: Int, expectedReplicas: Int) {
        val sourceResource = createStatefulSet()
        val patcher = NumSensorsLoadGeneratorReplicaPatcher(10000)
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            assertEquals(expectedReplicas, (resource as StatefulSet).spec.replicas)
        }
    }
}