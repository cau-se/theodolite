package rocks.theodolite.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Loads [KubernetesResource]s.
 */
fun loadKubernetesResources(resourceSet: List<ResourceSets>, client: NamespacedKubernetesClient): Collection<Pair<String, HasMetadata>> {
    return resourceSet.flatMap { it.loadResourceSet(client) }
}

@JsonDeserialize
@RegisterForReflection
class ResourceSets : KubernetesResource {
    @JsonProperty("configMap")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var configMap: ConfigMapResourceSet? = null

    @JsonProperty("fileSystem")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var fileSystem: FileSystemResourceSet? = null


    fun loadResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, HasMetadata>> {
        return if (this.configMap != null) {
            configMap?.getResourceSet(client = client)!!
        } else if (this.fileSystem != null) {
            fileSystem?.getResourceSet(client = client)!!
        } else {
            throw DeploymentFailedException("Could not load resourceSet.")
        }
    }
}