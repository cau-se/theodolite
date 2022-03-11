package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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
    fun deploy(resource: HasMetadata) {
        client.resource(resource).createOrReplace()
        /*
        when (resource) {
            is Deployment ->
                this.client.apps().deployments().createOrReplace(resource)
            is Service ->
                this.client.services().createOrReplace(resource)
            is ConfigMap ->
                this.client.configMaps().createOrReplace(resource)
            is StatefulSet ->
                this.client.apps().statefulSets().createOrReplace(resource)
            is CustomResourceWrapper -> resource.deploy(client)
            else -> throw IllegalArgumentException("Unknown Kubernetes resource.")
        }
        */
    }

    /**
     * Removes different k8s resources using the client.
     * @throws IllegalArgumentException if KubernetesResource not supported.
     */
    fun remove(resource: HasMetadata) {
        client.resource(resource).delete()
        when (resource) {
            is Deployment -> {
                //this.client.apps().deployments().delete(resource)
                ResourceByLabelHandler(client = client)
                    .blockUntilPodsDeleted(
                        matchLabels = resource.spec.selector.matchLabels
                    )
                logger.info { "Deployment '${resource.metadata.name}' deleted." }
            }
            /*
            is Service ->
                this.client.services().delete(resource)
            is ConfigMap ->
                this.client.configMaps().delete(resource)
            */
            is StatefulSet -> {
                //this.client.apps().statefulSets().delete(resource)
                ResourceByLabelHandler(client = client)
                    .blockUntilPodsDeleted(
                        matchLabels = resource.spec.selector.matchLabels
                    )
                logger.info { "StatefulSet '$resource.metadata.name' deleted." }
            }
            // is CustomResourceWrapper -> resource.delete(client)
            // else -> client.resource(resource).delete()
        }
    }
}
