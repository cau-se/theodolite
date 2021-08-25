package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromFile
import theodolite.util.DeploymentFailedException
import theodolite.util.YamlParserFromFile
import java.io.File

private val logger = KotlinLogging.logger {}

@RegisterForReflection
@JsonDeserialize
class FileSystemResourceSet: ResourceSet, KubernetesResource {
    lateinit var path: String
    lateinit var files: List<String>

    override fun getResourceSet(client: NamespacedKubernetesClient): List<Pair<String, KubernetesResource>> {

        //if files is set ...
        if(::files.isInitialized){
            return try {
                files
                    .map { loadSingleResource(resourceURL = it, client = client) }
            } catch (e: Exception) {
                throw  DeploymentFailedException("Could not load files located in $path")

            }
        }

        return try {
            File(path)
                .list() !!
                .filter { it.endsWith(".yaml") } // consider only yaml files, e.g. ignore readme files
                .map {
                    loadSingleResource(resourceURL = it, client = client)
                }
        } catch (e: Exception) {
            throw  DeploymentFailedException("Could not load files located in $path")
        }
    }

    private fun loadSingleResource(resourceURL: String, client: NamespacedKubernetesClient): Pair<String, KubernetesResource> {
        val parser = YamlParserFromFile()
        val loader = K8sResourceLoaderFromFile(client)
        val resourcePath = "$path/$resourceURL"
        return try {
            val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
            val k8sResource = loader.loadK8sResource(kind, resourcePath)
            Pair(resourceURL, k8sResource)
        } catch (e: Exception) {
            logger.error { "could not load resource: $resourcePath, caused by exception: $e" }
            throw e
        }
    }
}