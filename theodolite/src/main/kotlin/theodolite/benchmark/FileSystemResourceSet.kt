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
import java.io.FileNotFoundException
import java.lang.IllegalArgumentException

private val logger = KotlinLogging.logger {}

@RegisterForReflection
@JsonDeserialize
class FileSystemResourceSet: ResourceSet, KubernetesResource {
    lateinit var path: String
    lateinit var files: List<String>

    override fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, KubernetesResource>> {

        //if files is set ...
        if(::files.isInitialized){
            return files
                    .map { loadSingleResource(resourceURL = it, client = client) }
        }

        return try {
            File(path)
                .list() !!
                .filter { it.endsWith(".yaml") } // consider only yaml files, e.g. ignore readme files
                .map {
                    loadSingleResource(resourceURL = it, client = client)
                }
        } catch (e: NullPointerException) {
            throw  DeploymentFailedException("Could not load files located in $path, error is: ${e.message}")
        }
    }

    private fun loadSingleResource(resourceURL: String, client: NamespacedKubernetesClient): Pair<String, KubernetesResource> {
        val parser = YamlParserFromFile()
        val loader = K8sResourceLoaderFromFile(client)
        val resourcePath = "$path/$resourceURL"
        lateinit var kind: String

        try {
            kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
        } catch (e: NullPointerException) {
            throw DeploymentFailedException("Can not get Kind from resource $resourcePath, error is ${e.message}")
        } catch (e: FileNotFoundException){
            throw DeploymentFailedException("File $resourcePath not found")

        }

        return try {
            val k8sResource = loader.loadK8sResource(kind, resourcePath)
            Pair(resourceURL, k8sResource)
        } catch (e: IllegalArgumentException) {
            throw DeploymentFailedException("Could not load resource: $resourcePath, caused by exception: ${e.message}")
        }
    }
}