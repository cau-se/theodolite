package theodolite

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
private val logger = KotlinLogging.logger {}

class YamlLoader(client: NamespacedKubernetesClient) {
    var client = client

    fun loadService(path: String): Service? {

        var service: Service? = null

        try {
            service = client.services().load(path).get()
        }catch (e : Exception){
            logger.info("You potentially  misspeled the path: $path")
            logger.info("$e")
        }

        return service
    }
}

