package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromString
import theodolite.util.DeploymentFailedException
import theodolite.util.YamlParserFromString
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private val logger = KotlinLogging.logger {}

@RegisterForReflection
@JsonDeserialize
class ConfigMapResourceSet: ResourceSet, KubernetesResource {
    lateinit var name: String
    lateinit var files: List<String> // load all files, iff files is not set

    @OptIn(ExperimentalStdlibApi::class)
    override fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, KubernetesResource>> {
        val loader = K8sResourceLoaderFromString(client)
        var resources: Map<String, String>

        try {
            resources = client
                .configMaps()
                .withName(name)
                .get()
                .data
                .filter { it.key.endsWith(".yaml") } // consider only yaml files, e.g. ignore readme files
        } catch (e: KubernetesClientException) {
            throw DeploymentFailedException("can not find or read configmap:  $name", e)
        } catch (e: IllegalStateException) {
            throw DeploymentFailedException("can not find configmap or data section is null $name", e)
        }

        if (::files.isInitialized){
            resources = resources
                .filter { files.contains(it.key) }

            if (resources.size != files.size) {
                throw  DeploymentFailedException("Could not find all specified Kubernetes manifests files")
            }
        }

        return try {
            resources
                .map { Pair(
                    getKind(resource = it.value),
                    it) }
                .map {
                    Pair(
                        it.second.key,
                        loader.loadK8sResource(it.first, it.second.value)) }
        } catch (e: IllegalArgumentException) {
            throw  DeploymentFailedException("Can not creat resource set from specified configmap", e)
        }

    }

    private fun getKind(resource: String): String {
        val parser = YamlParserFromString()
        val resourceAsMap = parser.parse(resource, HashMap<String, String>()::class.java)

        return try {
            resourceAsMap?.get("kind") !!
        } catch (e: NullPointerException) {
            throw DeploymentFailedException( "Could not find field kind of Kubernetes resource: ${resourceAsMap?.get("name")}", e)
        }
    }
}