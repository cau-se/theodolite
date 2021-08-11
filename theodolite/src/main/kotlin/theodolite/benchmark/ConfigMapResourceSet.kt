package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import theodolite.k8s.K8sResourceLoaderFromString
import theodolite.util.YamlParserFromString
import kotlin.math.log

private val logger = KotlinLogging.logger {}



@JsonDeserialize
class ConfigMapResourceSet: ResourceSet {
    lateinit var configmap: String
    lateinit var files: List<String> // load all files, iff files is not set
    private val namespace = "default"
    private val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(namespace) // TODO(load namespace from env var)
    private val loader = K8sResourceLoaderFromString(client)


    @OptIn(ExperimentalStdlibApi::class)
    override fun getResourceSet(): List<Pair<String, KubernetesResource>> {
        logger.info { "Load benchmark resources from configmap with name $configmap" }

        var resources = client
            .configMaps()
            .withName(configmap)
            .get()
            .data


        if (::files.isInitialized){
            resources = resources
                .filterKeys { files.contains(it) }
        }

        return resources
            .map { Pair(
                getKind(resource = it.value),
                resources) }
            .map { Pair(
                it.first,
                loader.loadK8sResource(it.first, it.second.values.first())) }
    }

    private fun getKind(resource: String): String {
        logger.info { "1" }
        val parser = YamlParserFromString()
        val resoureceAsMap = parser.parse(resource, HashMap<String, String>()::class.java)
        logger.info { "2" }

        return try {
            val kind = resoureceAsMap?.get("kind")!!
            logger.info { "Kind is $kind" }
            kind

        } catch (e: Exception) {
            logger.error { "Could not find field kind of Kubernetes resource: ${resoureceAsMap?.get("name")}" }
            ""
        }
    }
}