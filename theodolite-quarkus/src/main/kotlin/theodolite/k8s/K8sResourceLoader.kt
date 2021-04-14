package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import theodolite.util.YamlParser

private val logger = KotlinLogging.logger {}

class K8sResourceLoader(private val client: NamespacedKubernetesClient) {

    /**
     * Parses a Service from a service yaml
     * @param path of the yaml file
     * @return service from fabric8
     */
    private fun loadService(path: String): Service {
        return loadGenericResource(path) { x: String -> client.services().load(x).get() }
    }

    /**
     * Parses a CustomResource from a yaml
     * @param path of the yaml file
     * @return customResource from fabric8
     */
    private fun loadServiceMonitor(path: String): ServiceMonitorWrapper {
        return loadGenericResource(path) { x: String -> ServiceMonitorWrapper(YamlParser().parse(path, HashMap<String, String>()::class.java)!!) }
    }

    /**
     * Parses a Deployment from a Deployment yaml
     * @param path of the yaml file
     * @return Deployment from fabric8
     */
    private fun loadDeployment(path: String): Deployment {
        return loadGenericResource(path) { x: String -> client.apps().deployments().load(x).get() }
    }

    /**
     * Parses a ConfigMap from a ConfigMap yaml
     * @param path of the yaml file
     * @return ConfigMap from fabric8
     */
    private fun loadConfigmap(path: String): ConfigMap {
        return loadGenericResource(path) { x: String -> client.configMaps().load(x).get() }
    }

    /**
     * Generic helper function to load a resource.
     * @param path of the resource
     * @param f function that shall be applied to the resource.
     */
    private fun <T> loadGenericResource(path: String, f: (String) -> T): T {
        var resource: T? = null

        try {
            resource = f(path)
        } catch (e: Exception) {
            logger.warn { "You potentially  misspelled the path: $path" }
            logger.warn { e }
        }

        if (resource == null) {
            throw IllegalArgumentException("The Resource at path: $path could not be loaded")
        }
        return resource
    }

    fun loadK8sResource(kind: String, path: String): KubernetesResource {
        return when (kind) {
            "Deployment" -> loadDeployment(path)
            "Service" -> loadService(path)
            "ServiceMonitor" -> loadServiceMonitor(path)
            "ConfigMap" -> loadConfigmap(path)
            else -> {
                logger.error { "Error during loading of unspecified resource Kind" }
                throw java.lang.IllegalArgumentException("error while loading resource with kind: $kind")
            }
        }
    }
}
