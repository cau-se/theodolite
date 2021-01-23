package theodolite

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
private val logger = KotlinLogging.logger {}

class YamlLoader(client: NamespacedKubernetesClient) {
    lateinit var client: NamespacedKubernetesClient

    init{
         this.client = client
    }


    fun loadService(path: String): Service? {

        var service: Service? = null

        try {
            service = client.services().load(path).get()
        } catch (e: Exception) {
            logger.info("You potentially  misspeled the path: $path")
            logger.info("$e")
        }

        return service
    }

    fun loadDeployment(path: String): Deployment? {
        val deployment = loadGenericRessource(path,{x: String-> client.apps().deployments().load(x).get()})
        return deployment
    }

    fun <T> loadGenericRessource(path: String, f: (String) -> T): T?{
        var service: T? = null

        try {
            service = f(path)
        } catch (e: Exception) {
            logger.info("You potentially  misspeled the path: $path")
            logger.info("$e")
        }

        return service
    }

    }
}
