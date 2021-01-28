package theodolite

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class ConfigMapManager(client: NamespacedKubernetesClient) {
    var client: NamespacedKubernetesClient

    init {
        this.client = client
    }

    fun deploy(configMap: ConfigMap) {
        client.configMaps().create(configMap)
    }

    fun delete(configMap: ConfigMap) {
        client.configMaps().delete(configMap)
    }
}
