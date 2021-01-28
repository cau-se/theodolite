package theodolite

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class YamlLoader(client: NamespacedKubernetesClient) {
    var client: NamespacedKubernetesClient

    init {
        this.client = client
    }

    /**
     * Parses a Service from a servive yaml
     * @param path of the yaml file
     * @return service from fabric8
     */
    fun loadService(path: String): Service? {

        val service = loadGenericRessource(path, { x: String -> client.services().load(x).get() })
        return service
    }

    /**
     * Parses a Deployment from a Deployment yaml
     * @param path of the yaml file
     * @return Deployment from fabric8
     */
    fun loadDeployment(path: String): Deployment? {
        val deployment = loadGenericRessource(path, { x: String -> client.apps().deployments().load(x).get() })
        return deployment
    }

    /**
     * Parses a ConfigMap from a ConfigMap yaml
     * @param path of the yaml file
     * @return ConfigMap from fabric8
     */
    fun loadConfigmap(path: String): ConfigMap? {
        val configMap = loadGenericRessource(path, { x: String -> client.configMaps().load(x).get() })
        return configMap
    }

    /**
     * Generic helper function to load a resource.
     * @param path of the resource
     * @param f fuction that shall be applied to the resource.
     */
    private fun <T> loadGenericRessource(path: String, f: (String) -> T): T? {
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
