package theodolite.k8s

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import java.lang.Exception

private val logger = KotlinLogging.logger {}

class ServiceMonitorWrapper(private val serviceMonitor: Map<String,String>): CustomResource() {

    /**
     * Deploy a service monitor
     *
     * @param client a namespaced Kubernetes client which are used to deploy the CR object.
     */
    fun deploy(client: NamespacedKubernetesClient) {
        val serviceMonitorContext = K8sContextFactory().create(api = "v1", scope = "Namespaced", group = "monitoring.coreos.com", plural = "servicemonitors")
        client.customResource(serviceMonitorContext)
            .createOrReplace(client.configuration.namespace, this.serviceMonitor as Map<String, Any>)
    }

    /**
     * Delete a service monitor
     *
     * @param client a namespaced Kubernetes client which are used to delete the CR object.
     */
    fun delete(client: NamespacedKubernetesClient) {
        val serviceMonitorContext = K8sContextFactory().create(api = "v1", scope = "Namespaced", group = "monitoring.coreos.com", plural = "servicemonitors")
        try {
            client.customResource(serviceMonitorContext).delete(client.configuration.namespace, this.getServiceMonitorName())
        } catch (e: Exception) {
            logger.warn { "Could not delete service monitor" }
        }
    }

    private fun getServiceMonitorName(): String {
        val smAsMap = this.serviceMonitor as Map<String, String>
        return smAsMap["name"] !!
    }
}