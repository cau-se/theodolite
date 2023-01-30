package rocks.theodolite.kubernetes.patcher

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
internal class PatcherFactoryTest {

    @Test
    fun testGenericResourcePatcherWithoutType() {
        val patcherDefinition = PatcherDefinition()
        patcherDefinition.type = "GenericResourcePatcher"
        patcherDefinition.properties = mapOf(
            "path" to "some/path/123/toSomeField"
        )
        val patcher = PatcherFactory.createPatcher(patcherDefinition)
        assertTrue(patcher is GenericResourcePatcher)
        val castedPatcher = patcher as GenericResourcePatcher
        assertEquals(listOf("some", "path", 123, "toSomeField"), castedPatcher.path)
        assertEquals(GenericResourcePatcher.Type.STRING, castedPatcher.type)
    }

    @Test
    fun testGenericResourcePatcherWithStringType() {
        val patcherDefinition = PatcherDefinition()
        patcherDefinition.type = "GenericResourcePatcher"
        patcherDefinition.properties = mapOf(
            "path" to "spec",
            "type" to "string"
        )
        val patcher = PatcherFactory.createPatcher(patcherDefinition)
        assertTrue(patcher is GenericResourcePatcher)
        val castedPatcher = patcher as GenericResourcePatcher
        assertEquals(GenericResourcePatcher.Type.STRING, castedPatcher.type)
    }

    @Test
    fun testGenericResourcePatcherWithBooleanType() {
        val patcherDefinition = PatcherDefinition()
        patcherDefinition.type = "GenericResourcePatcher"
        patcherDefinition.properties = mapOf(
            "path" to "spec",
            "type" to "boolean"
        )
        val patcher = PatcherFactory.createPatcher(patcherDefinition)
        assertTrue(patcher is GenericResourcePatcher)
        val castedPatcher = patcher as GenericResourcePatcher
        assertEquals(GenericResourcePatcher.Type.BOOLEAN, castedPatcher.type)
    }

    @Test
    fun testGenericResourcePatcherWithIntegerType() {
        val patcherDefinition = PatcherDefinition()
        patcherDefinition.type = "GenericResourcePatcher"
        patcherDefinition.properties = mapOf(
            "path" to "spec",
            "type" to "integer"
        )
        val patcher = PatcherFactory.createPatcher(patcherDefinition)
        assertTrue(patcher is GenericResourcePatcher)
        val castedPatcher = patcher as GenericResourcePatcher
        assertEquals(GenericResourcePatcher.Type.INTEGER, castedPatcher.type)
    }

    @Test
    fun testGenericResourcePatcherWithNumberType() {
        val patcherDefinition = PatcherDefinition()
        patcherDefinition.type = "GenericResourcePatcher"
        patcherDefinition.properties = mapOf(
            "path" to "spec",
            "type" to "number"
        )
        val patcher = PatcherFactory.createPatcher(patcherDefinition)
        assertTrue(patcher is GenericResourcePatcher)
        val castedPatcher = patcher as GenericResourcePatcher
        assertEquals(GenericResourcePatcher.Type.NUMBER, castedPatcher.type)
    }

}