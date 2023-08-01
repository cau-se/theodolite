package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * This class is used to deploy or remove different Kubernetes resources.
 * Supports: Deployments, Services, ConfigMaps, StatefulSets, and CustomResources.
 * @param client KubernetesClient used to deploy or remove.
 */
class K8sManager(private val client: KubernetesClient) {

    /**
     * Deploys different k8s resources using the client.
     * @throws IllegalArgumentException if KubernetesResource not supported.
     */
    fun deploy(resource: HasMetadata) {
        client.resource(resource).createOrReplace()
    }

    /**
     * Removes different k8s resources using the client.
     * @throws KubernetesClientException if an error occurs in the underlying NamespacedKubernetesClient when deleting the resource.
     */
    fun remove(resource: HasMetadata,  blockUntilDeleted: Boolean = true) {
        client.resource(resource).delete()
        if(blockUntilDeleted) {
            when (resource) {
                is Deployment -> {
                    ResourceByLabelHandler(client = client)
                        .blockUntilPodsDeleted(
                            matchLabels = resource.spec.selector.matchLabels
                        )
                    logger.info { "Deployment '${resource.metadata.name}' deleted." }
                }
                is StatefulSet -> {
                    ResourceByLabelHandler(client = client)
                        .blockUntilPodsDeleted(
                            matchLabels = resource.spec.selector.matchLabels
                        )
                    logger.info { "StatefulSet '$resource.metadata.name' deleted." }
                }
            }
        }
    }
}
