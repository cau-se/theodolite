package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import mu.KotlinLogging
import theodolite.util.YamlParser

private val logger = KotlinLogging.logger {}

/**
 * Used to load different Kubernetes resources.
 * Supports: Deployments, Services, ConfigMaps, and CustomResources.
 * @param client KubernetesClient used to deploy or remove.
 */
class K8sResourceLoader(private val client: NamespacedKubernetesClient) {

    /**
     * Parses a Service from a service yaml
     * @param path of the yaml file
     * @return Service from fabric8
     */
    private fun loadService(path: String): Service {
        return loadGenericResource(path) { x: String -> client.services().load(x).get() }
    }


    /**
     * Parses a CustomResource from a yaml
     * @param path of the yaml file
     * @param context specific crd context for this custom resource
     * @return  CustomResourceWrapper from fabric8
     */
   private fun loadCustomResourceWrapper(path: String, context: CustomResourceDefinitionContext): CustomResourceWrapper {
       return loadGenericResource(path) {
           CustomResourceWrapper(
               YamlParser().parse(
                   path,
                   HashMap<String, String>()::class.java
               )!!,
               context
           )
       }
   }

    private fun loadServiceMonitor(path: String): CustomResourceWrapper {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "monitoring.coreos.com",
            plural = "servicemonitors"
        )
        return loadCustomResourceWrapper(path, context)
    }

    private fun loadExecution(path: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "executions"
        )
        return loadCustomResourceWrapper(path, context)
    }

    private fun loadBenchmark(path: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "benchmarks"
        )
        return loadCustomResourceWrapper(path, context)
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
     * Parses a StatefulSet from a StatefulSet yaml
     * @param path of the yaml file
     * @return StatefulSet from fabric8
     */
    private fun loadStatefulSet(path: String): KubernetesResource {
        return loadGenericResource(path) { x: String -> client.apps().statefulSets().load(x).get() }

    }

    /**
     * Generic helper function to load a resource.
     * @param path of the resource
     * @param f function that is applied to the resource.
     * @throws IllegalArgumentException If the resource could not be loaded.
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
     * Supported kinds are: Deployments, Services, ServiceMonitors, ConfigMaps and CustomResources.
     * Uses CustomResource as default if Kind is not supported.
     * @param kind of the resource. CustomResource as default.
     * @param path of the resource to be loaded.
     * @throws Exception if the resource could not be loaded.
     */
    fun loadK8sResource(kind: String, path: String): KubernetesResource {
        return when (kind) {
            "Deployment" -> loadDeployment(path)
            "Service" -> loadService(path)
            "ServiceMonitor" -> loadServiceMonitor(path)
            "ConfigMap" -> loadConfigmap(path)
            "StatefulSet" -> loadStatefulSet(path)
            "Execution" -> loadExecution(path)
            "Benchmark" -> loadBenchmark(path)
            else -> {
                logger.error { "Error during loading of unspecified resource Kind" }
                throw java.lang.IllegalArgumentException("error while loading resource with kind: $kind")
            }
        }
    }
}
