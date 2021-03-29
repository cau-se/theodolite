package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import theodolite.util.YamlParser

private val logger = KotlinLogging.logger {}

/**
 * Used to load different k8s resources.
 * Supports: Deployments, Services, ConfigMaps, and CustomResources.
 * @param client - KubernetesClient used to deploy or remove.
 */
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
    private fun loadCustomResource(path: String): K8sCustomResourceWrapper {
        return loadGenericResource(path) { x: String ->
            K8sCustomResourceWrapper(YamlParser().parse(path, HashMap<String, String>()::class.java)!!)
        }
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

    /**
     * Factory function used to load different k8s resources from a path.
     * Supported kinds are: deployments,Services, ServiceMonitors,ConfigMaps and CustomResources.
     * Uses CustomResource as default if Kind is not supported.
     * @param kind - kind of the resource. CustomResource as default.
     * @param path - path of the resource to be loaded.
     */
    fun loadK8sResource(kind: String, path: String): KubernetesResource {
        return when (kind) {
            "Deployment" -> loadDeployment(path)
            "Service" -> loadService(path)
            "ServiceMonitor" -> loadCustomResource(path)
            "ConfigMap" -> loadConfigmap(path)
            else -> {
                logger.warn { "Try to load $kind from $path as Custom ressource" }
                try {
                    loadCustomResource(path)
                } catch (e: Exception) {
                    logger.error { "Error during loading of unspecified CustomResource: $e" }
                    throw e
                }
            }
        }
    }
}
