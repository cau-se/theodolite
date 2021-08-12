package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import mu.KotlinLogging
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromFile
import theodolite.util.DeploymentFailedException
import theodolite.util.YamlParserFromFile
import java.io.File

private val logger = KotlinLogging.logger {}
private const val DEFAULT_NAMESPACE = "default"

@JsonDeserialize
class FileSystemResourceSet: ResourceSet {
    lateinit var path: String
    lateinit var files: List<String>
    private val parser = YamlParserFromFile()
    private val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
    private val loader = K8sResourceLoaderFromFile(DefaultKubernetesClient().inNamespace(namespace))

    override fun getResourceSet(): List<Pair<String, KubernetesResource>> {

        //if files is set ...
        if(::files.isInitialized){
            return files
                .map { loadSingleResource(it)
                }
        }

        return try {
            File(path)
                .list() !!
                .filter { it.endsWith(".yaml") } // consider only yaml files, e.g. ignore readme files
                .map {
                    loadSingleResource(it)
                }
        } catch (e: Exception) {
            throw  DeploymentFailedException("Could not load files located in $path")
        }
    }

    private fun loadSingleResource(resourceURL: String): Pair<String, KubernetesResource> {
        val resourcePath = "$path/$resourceURL"
        val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
        val k8sResource = loader.loadK8sResource(kind, resourcePath)
        return Pair(resourceURL, k8sResource)
    }
}