package theodolite.benchmark

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.DeploymentFailedException

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