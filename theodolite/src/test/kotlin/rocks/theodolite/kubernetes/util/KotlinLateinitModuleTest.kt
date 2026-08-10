package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.model.BenchmarkExecution

internal class KotlinLateinitModuleTest {

    @Test
    fun `uninitialized lateinit String field serializes as null`() {
        val mapper = ObjectMapper().registerModule(KotlinLateinitModule())
        val execution = BenchmarkExecution() // benchmark is lateinit, not set
        val tree = mapper.readTree(mapper.writeValueAsString(execution))
        assertTrue(tree.get("benchmark").isNull)
    }

    @Test
    fun `initialized lateinit String field serializes correctly`() {
        val mapper = ObjectMapper().registerModule(KotlinLateinitModule())
        val execution = BenchmarkExecution().apply { benchmark = "my-benchmark" }
        val tree = mapper.readTree(mapper.writeValueAsString(execution))
        assertEquals("my-benchmark", tree.get("benchmark").asText())
    }

    @Test
    fun `uninitialized lateinit object field serializes as null`() {
        val mapper = ObjectMapper().registerModule(KotlinLateinitModule())
        val execution = BenchmarkExecution() // load, resources, execution, configOverrides not set
        val tree = mapper.readTree(mapper.writeValueAsString(execution))
        assertTrue(tree.get("load").isNull)
        assertTrue(tree.get("resources").isNull)
        assertTrue(tree.get("execution").isNull)
        assertTrue(tree.get("configOverrides").isNull)
    }

    @Test
    fun `without the module an uninitialized lateinit field throws during serialization`() {
        val mapper = ObjectMapper() // no KotlinLateinitModule
        val execution = BenchmarkExecution()
        assertThrows<Exception> { mapper.writeValueAsString(execution) }
    }
}
