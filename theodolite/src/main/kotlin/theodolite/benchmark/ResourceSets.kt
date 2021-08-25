package theodolite.benchmark

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.util.DeploymentFailedException

@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResourceSets: KubernetesResource {
    @JsonProperty("ConfigMapResourceSet")
    lateinit var  ConfigMapResourceSet: ConfigMapResourceSet

    @JsonProperty("FileSystemResourceSet")
    lateinit var FileSystemResourceSet: FileSystemResourceSet

    fun loadResourceSet(client: NamespacedKubernetesClient): List<Pair<String, KubernetesResource>> {
        return try {
            if (::ConfigMapResourceSet.isInitialized) {
                ConfigMapResourceSet.getResourceSet(client= client)
            } else if (::FileSystemResourceSet.isInitialized) {
                FileSystemResourceSet.getResourceSet(client= client )
            } else {
                throw  DeploymentFailedException("could not load resourceSet.")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}