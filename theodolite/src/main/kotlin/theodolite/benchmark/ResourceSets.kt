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
    lateinit var  configMap: ConfigMapResourceSet

    @JsonProperty("FileSystemResourceSet")
    lateinit var fileSystem: FileSystemResourceSet

    fun loadResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, KubernetesResource>> {
        return if (::configMap.isInitialized) {
            configMap.getResourceSet(client= client)
            } else if (::fileSystem.isInitialized) {
            fileSystem.getResourceSet(client= client )
            } else {
                throw  DeploymentFailedException("could not load resourceSet.")
            }
    }
}