package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class LabelPatcherTest: AbstractPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = LabelPatcher("labelName")
        value = "labelValue"
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun validate() {
        patch()
        resource.forEach {
            assertTrue((it as Deployment).metadata.labels.containsKey("labelName"))
            assertTrue(it.metadata.labels.get("labelName")=="labelValue")
        }
    }

    @Test
    fun getVariableName() {
    }
}