package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromString
import theodolite.util.DeploymentFailedException
import theodolite.util.YamlParserFromString
import java.lang.IllegalArgumentException

@RegisterForReflection
@JsonDeserialize
class ConfigMapResourceSet : ResourceSet, KubernetesResource {
    lateinit var name: String
    lateinit var files: List<String> // load all files, iff files is not set

    override fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, HasMetadata>> {
        val loader = K8sResourceLoaderFromString(client)
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

        if (::files.isInitialized) {
            val filteredResources = resources.filter { files.contains(it.key) }
            if (filteredResources.size != files.size) {
                throw DeploymentFailedException("Could not find all specified Kubernetes manifests files")
            }
            resources = filteredResources
        }

        return try {
            resources
                .map {
                    Pair(
                        getKind(resourceYaml = it.value),
                        it
                    )
                }
                .map {
                    Pair(
                        it.second.key, // filename
                        client.resource(it.second.value).get()
                        //loader.loadK8sResource(kind = it.first, resourceString = it.second.value) // K8s resource
                    )
                }
        } catch (e: IllegalArgumentException) {
            throw DeploymentFailedException("Cannot create resource set from specified ConfigMap", e)
        }

    }

    private fun getKind(resourceYaml: String): String {
        val parser = YamlParserFromString()
        val resourceAsMap = parser.parse(resourceYaml, HashMap<String, String>()::class.java)

        return resourceAsMap?.get("kind")
            ?: throw DeploymentFailedException("Could not find 'kind' field of Kubernetes resource: ${resourceAsMap?.get("name")}")
    }
}