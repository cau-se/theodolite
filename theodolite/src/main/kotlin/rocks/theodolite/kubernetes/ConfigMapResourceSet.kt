package rocks.theodolite.kubernetes

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import java.lang.IllegalArgumentException

@RegisterForReflection
@JsonDeserialize
class ConfigMapResourceSet : ResourceSet, KubernetesResource {
    lateinit var name: String
    var files: List<String>? = null // load all files, iff files is not set

    override fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, HasMetadata>> {
        var resources: Map<String, String>

        try {
            resources = (client
                .configMaps()
                .withName(name)
                .get() ?: throw DeploymentFailedException("Cannot find ConfigMap with name '$name'."))
                .data
                .filter { it.key.endsWith(".yaml") || it.key.endsWith(".yml")}
        } catch (e: KubernetesClientException) {
            throw DeploymentFailedException("Cannot find or read ConfigMap with name '$name'.", e)
        }

        files?.run {
            val filteredResources = resources.filter { this.contains(it.key) }
            if (filteredResources.size != this.size) {
                throw DeploymentFailedException("Could not find all specified Kubernetes manifests files")
            }
            resources = filteredResources
        }

        return try {
            resources
                .map {
                    Pair(
                        it.key, // filename
                        client.resource(it.value).item()
                    )
                }
        } catch (e: IllegalArgumentException) {
            throw DeploymentFailedException("Cannot create resource set from specified ConfigMap", e)
        }

    }

}