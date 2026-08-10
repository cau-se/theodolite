package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.client.utils.KubernetesSerialization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

internal class KotlinLateinitModuleTest {

    private fun mapperWithModule() = ObjectMapper().registerModule(KotlinLateinitModule())

    @Test
    fun `uninitialized lateinit String property is serialized as null`() {
        val mapper = mapperWithModule()
        val json = mapper.readTree(mapper.writeValueAsString(BenchmarkExecution()))
        assertTrue(json.get("benchmark").isNull)
    }

    @Test
    fun `initialized lateinit String property is serialized normally`() {
        val mapper = mapperWithModule()
        val execution = BenchmarkExecution().apply { benchmark = "my-benchmark" }
        val json = mapper.readTree(mapper.writeValueAsString(execution))
        assertEquals("my-benchmark", json.get("benchmark").asText())
    }

    @Test
    fun `uninitialized lateinit object properties are serialized as null`() {
        val mapper = mapperWithModule()
        val json = mapper.readTree(mapper.writeValueAsString(BenchmarkExecution()))
        assertTrue(json.get("load").isNull)
        assertTrue(json.get("resources").isNull)
        assertTrue(json.get("execution").isNull)
        assertTrue(json.get("configOverrides").isNull)
    }

    @Test
    fun `properties without lateinit are not affected`() {
        val mapper = mapperWithModule()
        val json = mapper.readTree(mapper.writeValueAsString(BenchmarkExecution()))
        assertEquals(0, json.get("executionId").asInt())
        assertTrue(json.get("slis").isNull)
        assertTrue(json.get("slos").isNull)
    }

    @Test
    fun `serialization fails without the module`() {
        val mapper = ObjectMapper()
        assertThrows<Exception> { mapper.writeValueAsString(BenchmarkExecution()) }
    }

    /**
     * Reproduces the failure of the Java Operator SDK adding a finalizer to an Execution whose spec
     * misses required fields. The Kubernetes client uses its own object mapper, so registering the
     * module on the general-purpose one is not sufficient.
     */
    @Test
    fun `custom resource with uninitialized spec is serialized by the Kubernetes client`() {
        val serialization = KubernetesSerialization(mapperWithModule(), true)
        val crd = ExecutionCRD().apply { metadata.name = "test-execution" }

        val json = ObjectMapper().readTree(serialization.asJson(crd))

        assertEquals("test-execution", json.at("/metadata/name").asText())
        assertTrue(json.at("/spec/benchmark").isNull)
    }

    @Test
    fun `custom resource with uninitialized spec fails without the module`() {
        val serialization = KubernetesSerialization(ObjectMapper(), true)
        val crd = ExecutionCRD().apply { metadata.name = "test-execution" }

        assertThrows<Exception> { serialization.asJson(crd) }
    }
}
