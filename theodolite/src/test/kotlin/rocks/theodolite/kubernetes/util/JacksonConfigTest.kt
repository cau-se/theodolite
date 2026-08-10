package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.client.utils.KubernetesSerialization
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD

/**
 * Verifies that the [KotlinLateinitModule] is registered on the object mapper that the Kubernetes
 * client actually uses, which is not the general-purpose CDI object mapper.
 */
@WithKubernetesTestServer
@QuarkusTest
internal class JacksonConfigTest {

    @Inject
    lateinit var serialization: KubernetesSerialization

    @Test
    fun `Kubernetes client serializes a custom resource with an uninitialized spec`() {
        val crd = ExecutionCRD().apply { metadata.name = "test-execution" }

        val json = ObjectMapper().readTree(this.serialization.asJson(crd))

        assertEquals("test-execution", json.at("/metadata/name").asText())
        // The spec must not contain any field, as the Kubernetes API server rejects fields that are
        // not declared in the CRD schema and server-side apply treats null as a field deletion.
        assertTrue(json.at("/spec").isObject)
        assertFalse(json.at("/spec").fieldNames().hasNext())
    }
}
