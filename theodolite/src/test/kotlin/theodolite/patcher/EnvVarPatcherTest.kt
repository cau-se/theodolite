package theodolite.patcher

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.EnvVarBuilder
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

@QuarkusTest
internal class EnvVarPatcherTest : AbstractPatcherTest() {

    @BeforeEach
    fun setUp() {
        resource = listOf(createDeployment())
        patcher = EnvVarPatcher(variableName = "testEnv", container = "container")
        value = "testValue"
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    override fun validate() {
        patch()
        val envVar = EnvVarBuilder().withName("testEnv").withValue("testValue").build()
        resource.forEach {
        assertTrue((it as Deployment).spec.template.spec.containers[0].env.contains(envVar))
        }
    }
}