package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class K8sManager(private val client: NamespacedKubernetesClient) {
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
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }

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
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
    }
}
