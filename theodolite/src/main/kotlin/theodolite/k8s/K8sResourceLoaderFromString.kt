package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.utils.Serialization
import mu.KotlinLogging
import theodolite.util.YamlParserFromString
import java.io.ByteArrayInputStream
private val logger = KotlinLogging.logger {}



class K8sResourceLoaderFromString(private val client: NamespacedKubernetesClient) {

    @OptIn(ExperimentalStdlibApi::class)
    fun loadK8sResource(kind: String, resourceString: String): KubernetesResource {

        return when (kind) {
            "Deployment" -> loadDeployment(resourceString)
            "Service" -> loadService(resourceString)
            //"ServiceMonitor" -> loadServiceMonitor(resourceString) // TODO(Add support for custom resources)
            "ConfigMap" -> loadConfigmap(resourceString)
            "StatefulSet" -> loadStatefulSet(resourceString)
            //"Execution" -> loadExecution(resourceString)
            //"Benchmark" -> loadBenchmark(resourceString)
            else -> {
                logger.error { "Error during loading of unspecified resource Kind" }
                throw java.lang.IllegalArgumentException("error while loading resource with kind: $kind")
            }
        }
    }

    /**
     * Generic helper function to load a resource.
     * @param path of the resource
     * @param f function that is applied to the resource.
     * @throws IllegalArgumentException If the resource could not be loaded.
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun <T> loadGenericResource(resourceString: String, f: (ByteArrayInputStream) -> T): T {
        val stream = ByteArrayInputStream(resourceString.encodeToByteArray())
        var resource: T? = null

        try {
            resource = f(stream)
        } catch (e: Exception) {
            logger.warn { "You potentially  misspelled the path: ....1" }
            logger.warn { e }
        }

        if (resource == null) {
            throw IllegalArgumentException("The Resource: ....1 could not be loaded")
        }
        return resource
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun loadService(resourceStream: String): KubernetesResource {

        //logger.info { resourceStream }

        val stream = ByteArrayInputStream(resourceStream.encodeToByteArray())
        //val test = Serialization.unmarshal<Service>(stream, Service::class.java)
        //logger.info { test }
        // return test
        logger.info { "Test" }
        //val parser = YamlParserFromString()
        //val resoureceAsMap = parser.parse(resourceStream, HashMap<String, String>()::class.java)
        //val loadedSvc: Service = client.services().load(stream).get()
        //logger.info { "loadedSvc" }
        //return loadedSvc
        //logger.info { "try to load service" }
        return loadGenericResource(resourceStream) { x: ByteArrayInputStream -> client.services().load(x).get() }
    }

    private fun loadDeployment(path: String): Deployment {
        return loadGenericResource(path) { x: ByteArrayInputStream -> client.apps().deployments().load(x).get() }
    }

    private fun loadConfigmap(path: String): ConfigMap {
        return loadGenericResource(path) { x: ByteArrayInputStream -> client.configMaps().load(x).get() }
    }

    private fun loadStatefulSet(path: String): KubernetesResource {
        return loadGenericResource(path) { x: ByteArrayInputStream -> client.apps().statefulSets().load(x).get() }
    }
}