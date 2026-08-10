package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.kubernetes.client.KubernetesClientObjectMapperCustomizer
import jakarta.inject.Singleton

/**
 * Registers the [KotlinLateinitModule] on the object mapper of the Kubernetes client, which is the
 * mapper that serializes custom resources for API server requests. Note that this is a different
 * mapper than the general-purpose one provided by Quarkus.
 */
@Singleton
class TheodoliteKubernetesClientObjectMapperCustomizer : KubernetesClientObjectMapperCustomizer {
    override fun customize(mapper: ObjectMapper) {
        mapper.registerModule(KotlinLateinitModule())
    }
}
