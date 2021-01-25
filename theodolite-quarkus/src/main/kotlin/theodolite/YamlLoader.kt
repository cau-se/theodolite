package theodolite

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class YamlLoader(client: NamespacedKubernetesClient) {
    lateinit var client: NamespacedKubernetesClient

    init {
        this.client = client
    }

    fun loadService(path: String): Service? {

        val service = loadGenericRessource(path, { x: String -> client.services().load(x).get() })
        return service
    }

    fun loadDeployment(path: String): Deployment? {
        val deployment = loadGenericRessource(path, { x: String -> client.apps().deployments().load(x).get() })
        return deployment
    }

    fun loadConfigmap(path: String): ConfigMap? {
        val configMap = loadGenericRessource(path, { x: String -> client.configMaps().load(x).get() })
        return configMap
    }

    fun <T> loadGenericRessource(path: String, f: (String) -> T): T? {
        var resource: T? = null

        try {
            resource = f(path)
        } catch (e: Exception) {
            logger.info("You potentially  misspelled the path: $path")
            logger.info("$e")
        }
        return resource
    }
}
