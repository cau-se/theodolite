package theodolite.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import mu.KotlinLogging
import theodolite.k8s.K8sContextFactory

private val logger = KotlinLogging.logger {}

abstract class AbstractK8sLoader: K8sResourceLoader {

    abstract fun loadCustomResourceWrapper(path: String, context: CustomResourceDefinitionContext): KubernetesResource

    fun loadK8sResource(kind: String, resourceString: String): KubernetesResource {
        return when (kind) {
            "Deployment" -> loadDeployment(resourceString)
            "Service" -> loadService(resourceString)
            "ServiceMonitor" -> loadServiceMonitor(resourceString)
            "ConfigMap" -> loadConfigmap(resourceString)
            "StatefulSet" -> loadStatefulSet(resourceString)
            "Execution" -> loadExecution(resourceString)
            "Benchmark" -> loadBenchmark(resourceString)
            else -> {
                logger.error { "Error during loading of unspecified resource Kind" }
                throw java.lang.IllegalArgumentException("error while loading resource with kind: $kind")
            }
        }
    }

    fun <T> loadGenericResource(resourceString: String, f: (String) -> T): T {
        var resource: T? = null

        try {
            resource = f(resourceString)
        } catch (e: Exception) {
            logger.warn { "You potentially  misspelled the path: ....1" }
            logger.warn { e }
        }

        if (resource == null) {
            throw IllegalArgumentException("The Resource: ....1 could not be loaded")
        }
        return resource
    }



    override fun loadServiceMonitor(path: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "monitoring.coreos.com",
            plural = "servicemonitors"
        )
        return loadCustomResourceWrapper(path, context)
    }

    override fun loadExecution(path: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "executions"
        )
        return loadCustomResourceWrapper(path, context)
    }

    override fun loadBenchmark(path: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "benchmarks"
        )
        return loadCustomResourceWrapper(path, context)
    }
}