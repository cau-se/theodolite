package theodolite

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class YamlLoader(client: NamespacedKubernetesClient) {
    var client = client

    fun loadService(path: String): Service? {

        var service: Service? = null

        try {
            service = client.services().load(path).get()
        }catch (e : Exception){
        }

        return service
    }
}

