package theodolite.k8s.resourceLoader

import io.fabric8.kubernetes.api.model.KubernetesResource
import mu.KotlinLogging
import theodolite.k8s.K8sContextFactory

private val logger = KotlinLogging.logger {}

abstract class AbstractK8sLoader: K8sResourceLoader {

    fun loadK8sResource(kind: String, resourceString: String): KubernetesResource {
        return when (kind.replaceFirst(kind[0],kind[0].uppercaseChar())) {
            "Deployment" -> loadDeployment(resourceString)
            "Service" -> loadService(resourceString)
            "ServiceMonitor" -> loadServiceMonitor(resourceString)
            "ConfigMap" -> loadConfigmap(resourceString)
            "StatefulSet" -> loadStatefulSet(resourceString)
            "Execution" -> loadExecution(resourceString)
            "Benchmark" -> loadBenchmark(resourceString)
            else -> {
                logger.error { "Error during loading of unspecified resource Kind '$kind'." }
                throw IllegalArgumentException("error while loading resource with kind: $kind")
            }
        }
    }

    fun <T> loadGenericResource(resourceString: String, f: (String) -> T): T {
        var resource: T? = null

        try {
            resource = f(resourceString)
        } catch (e: Exception) {
            logger.warn { e }
        }

        if (resource == null) {
            throw IllegalArgumentException("The Resource: $resourceString could not be loaded")
        }
        return resource
    }



    override fun loadServiceMonitor(resource: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "monitoring.coreos.com",
            plural = "servicemonitors"
        )
        return loadCustomResourceWrapper(resource, context)
    }

    override fun loadExecution(resource: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "executions"
        )
        return loadCustomResourceWrapper(resource, context)
    }

    override fun loadBenchmark(resource: String): KubernetesResource {
        val context = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "theodolite.com",
            plural = "benchmarks"
        )
        return loadCustomResourceWrapper(resource, context)
    }
}