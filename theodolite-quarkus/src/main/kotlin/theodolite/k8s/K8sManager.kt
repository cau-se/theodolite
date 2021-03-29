package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

/**
 * This class is used to deploy or remove different k8s resources.
 * Supports: Deployments, Services, ConfigMaps, StatefulSets, and CustomResources.
 * @param client - KubernetesClient used to deploy or remove.
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
            is K8sCustomResourceWrapper -> resource.deploy(client)
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }

    /**
     * Removes different k8s resources using the client.
     * @throws IllegalArgumentException if KubernetesResource not supported.
     */
    fun remove(resource: KubernetesResource) {
        when (resource) {
            is Deployment ->
                this.client.apps().deployments().delete(resource)
            is Service ->
                this.client.services().delete(resource)
            is ConfigMap ->
                this.client.configMaps().delete(resource)
            is StatefulSet ->
                this.client.apps().statefulSets().delete(resource)
            is K8sCustomResourceWrapper -> resource.delete(client)
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }
}
