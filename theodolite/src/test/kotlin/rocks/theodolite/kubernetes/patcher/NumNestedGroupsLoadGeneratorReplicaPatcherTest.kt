package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@QuarkusTest
internal class NumNestedGroupsLoadGeneratorReplicaPatcherTest : AbstractPatcherTest(){

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = NumNestedGroupsLoadGeneratorReplicaPatcher("10", "500")
        value = "2"
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    override fun validate() {
        patch()
        resource.forEach {
            assertTrue((it as Deployment).spec.replicas == 1)
        }
    }
}