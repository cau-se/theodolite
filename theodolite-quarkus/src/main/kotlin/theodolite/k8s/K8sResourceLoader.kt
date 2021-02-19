package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class K8sResourceLoader(private val client: NamespacedKubernetesClient) {

    /**
     * Parses a Service from a servive yaml
     * @param path of the yaml file
     * @return service from fabric8
     */
    fun loadService(path: String): Service {
        return loadGenericRessource(path) { x: String -> client.services().load(x).get() }
    }

    /**
     * Parses a Service from a servive yaml
     * @param path of the yaml file
     * @return service from fabric8
     */
    fun loadServiceMonitor(path: String): CustomResourceDefinition {
        return loadGenericRessource(path) { x: String -> client.customResourceDefinitions().load(x).get() }
    }

    /**
     * Parses a Deployment from a Deployment yaml
     * @param path of the yaml file
     * @return Deployment from fabric8
     */
    fun loadDeployment(path: String): Deployment {
        return loadGenericRessource(path) { x: String -> client.apps().deployments().load(x).get() }
    }

    /**
     * Parses a ConfigMap from a ConfigMap yaml
     * @param path of the yaml file
     * @return ConfigMap from fabric8
     */
    fun loadConfigmap(path: String): ConfigMap {
        return loadGenericRessource(path) { x: String -> client.configMaps().load(x).get() }
    }

    /**
     * Generic helper function to load a resource.
     * @param path of the resource
     * @param f fuction that shall be applied to the resource.
     */
    private fun <T> loadGenericRessource(path: String, f: (String) -> T): T {
        var resource: T? = null

        try {
            resource = f(path)
        } catch (e: Exception) {
            logger.info("You potentially  misspelled the path: $path")
            logger.info("$e")
        }

        if (resource == null) {
            throw IllegalArgumentException("The Resource at path: $path could not be loaded")
        }

        return resource
    }

    public fun loadK8sResource(kind: String, path: String): KubernetesResource {
        return when (kind){
            "Deployment" -> loadDeployment(path)
            "Service" -> loadService(path)
            "ServiceMonitor" -> loadServiceMonitor(path)
            "ConfigMap" -> loadConfigmap(path)
            else -> return loadConfigmap(path) // throw java.lang.IllegalArgumentException("Resource typ $kind not known")
        }
    }
}
