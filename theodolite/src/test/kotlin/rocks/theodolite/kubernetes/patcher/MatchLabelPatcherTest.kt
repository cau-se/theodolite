package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class MatchLabelPatcherTest: AbstractPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = MatchLabelPatcher("labelName")
        value = "labelValue"
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    override fun validate() {
        patch()
        resource.forEach {
            assertTrue((it as Deployment).spec.selector.matchLabels.containsKey("labelName"))
            assertTrue(it.spec.selector.matchLabels.get("labelName")=="labelValue")
        }
    }

    @Test
    fun getVariableName() {
    }
}