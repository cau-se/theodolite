package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromString
import theodolite.util.YamlParserFromString

private val logger = KotlinLogging.logger {}
private const val DEFAULT_NAMESPACE = "default"

@RegisterForReflection
@JsonDeserialize
class ConfigMapResourceSet: ResourceSet, KubernetesResource {
    lateinit var configmap: String
    lateinit var files: List<String> // load all files, iff files is not set

    @OptIn(ExperimentalStdlibApi::class)
    override fun getResourceSet(): List<Pair<String, KubernetesResource>> {
        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(namespace)
        val loader = K8sResourceLoaderFromString(client)

        var resources = client
            .configMaps()
            .withName(configmap)
            .get()
            .data
            .filter { it.key.endsWith(".yaml") } // consider only yaml files, e.g. ignore readme files


        if (::files.isInitialized){
            resources = resources
                .filter { files.contains(it.key) }
        }

        return resources
            .map { Pair(
                getKind(resource = it.value),
                it) }
            .map {
                Pair(
                it.second.key,
                loader.loadK8sResource(it.first, it.second.value)) }
    }

    private fun getKind(resource: String): String {
        val parser = YamlParserFromString()
        val resourceAsMap = parser.parse(resource, HashMap<String, String>()::class.java)

        return try {
            resourceAsMap?.get("kind")!!
        } catch (e: Exception) {
            logger.error { "Could not find field kind of Kubernetes resource: ${resourceAsMap?.get("name")}" }
            ""
        }
    }
}