package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class TemplateLabelPatcherTest: AbstractStringPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = TemplateLabelPatcher( "labelName")
        value = "labelValue"
    }


    @Test
    override fun validate() {
        patch()
        resource.forEach {
            assertTrue((it as Deployment).spec.template.metadata.labels.containsKey("labelName"))
            assertTrue(it.spec.template.metadata.labels["labelName"] =="labelValue")
        }
    }

}