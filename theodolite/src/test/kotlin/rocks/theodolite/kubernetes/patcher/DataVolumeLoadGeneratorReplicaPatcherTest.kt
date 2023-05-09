package theodolite.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.patcher.AbstractPatcherTest
import rocks.theodolite.kubernetes.patcher.DataVolumeLoadGeneratorReplicaPatcher

import rocks.theodolite.kubernetes.patcher.VolumesConfigMapPatcher

@QuarkusTest
internal class DataVolumeLoadGeneratorReplicaPatcherTest: AbstractPatcherTest() {

    @Test
    fun testFullyUtilizedGenerators() {
        testInputVsExpected(50000, 5, 10000)
    }

    @Test
    fun testNonFullyUtilizedGenerators() {
        testInputVsExpected(45000, 5, 9000)
    }

    private fun testInputVsExpected(inputLoad: Int, expectedReplicas: Int, expectedEnvVar: Int) {
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

}