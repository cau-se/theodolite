package theodolite.deprecated

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class ServiceManager(private val client: NamespacedKubernetesClient) {

    fun changeServiceName(service: Service, newName: String) {

        service.metadata.apply {
            name = newName
        }
    }

    fun deploy(service: Service) {
        client.services().createOrReplace(service)
    }

    fun delete(service: Service) {
        client.services().delete(service)
    }
}
