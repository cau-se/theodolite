package theodolite.k8s

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class ServiceManager(client: NamespacedKubernetesClient) {
    var client: NamespacedKubernetesClient

    init {
        this.client = client
    }

    fun changeServiceName(service: Service, newName: String) {

        service.metadata.apply {
            name = newName
        }
    }

    fun deploy(service: Service) {
        client.services().create(service)
    }

    fun delete(service: Service) {
        client.services().delete(service)
    }
}
