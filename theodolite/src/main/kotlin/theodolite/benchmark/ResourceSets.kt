package theodolite.benchmark

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.util.DeploymentFailedException

@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResourceSets: KubernetesResource {
    @JsonProperty("ConfigMapResourceSet")
    val ConfigMapResourceSet: ConfigMapResourceSet? = null

    @JsonProperty("FileSystemResourceSet")
    val FileSystemResourceSet: FileSystemResourceSet? = null

    fun loadResourceSet(): List<Pair<String, KubernetesResource>> {
        return try {
            if (ConfigMapResourceSet != null) {
                ConfigMapResourceSet.getResourceSet()
            } else if (FileSystemResourceSet != null) {
                FileSystemResourceSet.getResourceSet()
            } else {
                throw  DeploymentFailedException("could not load resourceSet.")
            }
        } catch (e: Exception) {
            throw e
        }

    }
}