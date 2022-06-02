package theodolite.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class DataVolumeLoadGeneratorReplicaPatcherTest: AbstractPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = VolumesConfigMapPatcher((resource.first() as Deployment).spec.template.spec.volumes[0].configMap.name)
        value = "new-configMapName"
    }

    @Test
    override fun validate() {
        patch()
        resource.forEach {
            assert((it as Deployment).spec.template.spec.volumes[0].configMap.name == value)
        }
    }
}