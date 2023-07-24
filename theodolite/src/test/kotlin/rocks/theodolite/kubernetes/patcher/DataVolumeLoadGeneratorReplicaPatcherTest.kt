package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusTest
internal class DataVolumeLoadGeneratorReplicaPatcherTest: AbstractPatcherTest() {

    @Test
    fun testFullyUtilizedGeneratorDeployment() {
        testInputVsExpectedForDeployment(50000, 5, 10000)
    }

    @Test
    fun testNonFullyUtilizedGeneratorDeployment() {
        testInputVsExpectedForDeployment(45000, 5, 9000)
    }

    @Test
    fun testFullyUtilizedGeneratorStatefulSet() {
        testInputVsExpectedForSts(50000, 5, 10000)
    }

    @Test
    fun testNonFullyUtilizedGeneratorStatefulSet() {
        testInputVsExpectedForSts(45000, 5, 9000)
    }

    private fun testInputVsExpectedForDeployment(inputLoad: Int, expectedReplicas: Int, expectedEnvVar: Int) {
        val sourceResource = createDeployment()
        val patcher = DataVolumeLoadGeneratorReplicaPatcher(
                10000,
                sourceResource.spec.template.spec.containers[0].name,
                "RECORDS_PER_SECOND"
        )
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            val envVar = (resource as Deployment).spec.template.spec.containers[0].env
                    .find { it.name == "RECORDS_PER_SECOND" }
            Assertions.assertNotNull(envVar)
            Assertions.assertEquals(expectedEnvVar.toString(), envVar!!.value)
            Assertions.assertEquals(expectedReplicas, resource.spec.replicas)
        }
    }

    private fun testInputVsExpectedForSts(inputLoad: Int, expectedReplicas: Int, expectedEnvVar: Int) {
        val sourceResource = createStatefulSet()
        val patcher = DataVolumeLoadGeneratorReplicaPatcher(
                10000,
                sourceResource.spec.template.spec.containers[0].name,
                "RECORDS_PER_SECOND"
        )
        val patchedResource = patcher.patch(listOf(sourceResource), inputLoad.toString())
        patchedResource.forEach { resource ->
            val envVar = (resource as StatefulSet).spec.template.spec.containers[0].env
                    .find { it.name == "RECORDS_PER_SECOND" }
            Assertions.assertNotNull(envVar)
            Assertions.assertEquals(expectedEnvVar.toString(), envVar!!.value)
            Assertions.assertEquals(expectedReplicas, resource.spec.replicas)
        }
    }

}