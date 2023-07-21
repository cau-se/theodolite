package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Service
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ServiceSelectorPatcherTest: AbstractPatcherTest() {

    @Test
    fun testService() {
        val sourceResource = createService()
        val patcher = ServiceSelectorPatcher("some-label")
        val patchedResources = patcher.patch(listOf(sourceResource), "some-value")
        patchedResources.forEach {
            assertTrue((it as Service).spec.selector.containsKey("some-label"))
            assertEquals("some-value", it.spec.selector["some-label"])
        }
    }

}