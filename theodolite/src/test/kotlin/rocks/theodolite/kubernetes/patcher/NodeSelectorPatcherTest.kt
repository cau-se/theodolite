package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class NodeSelectorPatcherTest: AbstractPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = NodeSelectorPatcher("nodeName")
        value = "nodeValue"

    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun validate() {
        patch()
        resource.forEach {
            assertTrue((it as Deployment).spec.template.spec.nodeSelector.containsKey("nodeName"))
            assertTrue(it.spec.template.spec.nodeSelector["nodeName"] == value)
        }
    }

}