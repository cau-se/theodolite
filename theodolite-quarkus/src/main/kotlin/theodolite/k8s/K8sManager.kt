package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

/**
 * This class is used to deploy or remove different Kubernetes resources.
 * Supports: Deployments, Services, ConfigMaps, StatefulSets, and CustomResources.
 * @param client KubernetesClient used to deploy or remove.
 */
class K8sManager(private val client: NamespacedKubernetesClient) {

    /**
     * Deploys different k8s resources using the client.
     * @throws IllegalArgumentException if KubernetesResource not supported.
     */
    fun deploy(resource: KubernetesResource) {
        when (resource) {
            is Deployment ->
                this.client.apps().deployments().createOrReplace(resource)
            is Service ->
                this.client.services().createOrReplace(resource)
            is ConfigMap ->
                this.client.configMaps().createOrReplace(resource)
            is StatefulSet ->
                this.client.apps().statefulSets().createOrReplace(resource)
            is ServiceMonitorWrapper -> resource.deploy(client)
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }

    /**
     * Removes different k8s resources using the client.
     * @throws IllegalArgumentException if KubernetesResource not supported.
     */
    fun remove(resource: KubernetesResource) {
        when (resource) {
            is Deployment -> {
                val label = resource.spec.selector.matchLabels["app"]!!
                this.client.apps().deployments().delete(resource)
                blockUntilDeleted(label)
            }
            is Service ->
                this.client.services().delete(resource)
            is ConfigMap ->
                this.client.configMaps().delete(resource)
            is StatefulSet -> {
                val label = resource.spec.selector.matchLabels["app"]!!
                this.client.apps().statefulSets().delete(resource)
                blockUntilDeleted(label)
            }
            is ServiceMonitorWrapper -> resource.delete(client)
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }


    private fun blockUntilDeleted(label: String) {
        var deleted = false
        do {
            val pods = this.client.pods().withLabel(label).list().items
            if (pods.isNullOrEmpty()) {
                deleted = true
            }
            Thread.sleep(1000)
        } while (!deleted)
    }

}
