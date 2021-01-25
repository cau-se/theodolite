package theodolite

import io.fabric8.kubernetes.api.model.Service

class ServiceManager {
    fun changeServiceName(service: Service, newName: String) {

        service.metadata.apply {
            name = newName
        }
    }
}
