package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class ConfigMapManager(private val client: NamespacedKubernetesClient) {

    fun deploy(configMap: ConfigMap) {
        this.client.configMaps().createOrReplace(configMap)
    }

    fun delete(configMap: ConfigMap) {
        this.client.configMaps().delete(configMap)
    }
}
