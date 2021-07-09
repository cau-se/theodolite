package theodolite.k8s

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CustomResourceWrapper(val crAsMap: Map<String, String>, private val context: CustomResourceDefinitionContext) : KubernetesResource {
    /**
     * Deploy a service monitor
     *
     * @param client a namespaced Kubernetes client which are used to deploy the CR object.
     *
     * @throws java.io.IOException if the resource could not be deployed.
     */
    fun deploy(client: NamespacedKubernetesClient) {
        client.customResource(this.context)
            .createOrReplace(client.configuration.namespace, this.crAsMap as Map<String, Any>)
    }

    /**
     * Delete a service monitor
     *
     * @param client a namespaced Kubernetes client which are used to delete the CR object.
     */
    fun delete(client: NamespacedKubernetesClient) {
        try {
            client.customResource(this.context)
                .delete(client.configuration.namespace, this.getName())
        } catch (e: Exception) {
            logger.warn { "Could not delete service monitor" }
        }
    }

    /**
     * @throws NullPointerException if name or metadata is null
     */
    fun getName(): String {
        val metadataAsMap = this.crAsMap["metadata"]!! as Map<String, String>
        return metadataAsMap["name"]!!
    }

    fun getLabels(): Map<String, String>{
        val metadataAsMap = this.crAsMap["metadata"]!! as Map<String, String>
        return metadataAsMap["labels"]!! as Map<String, String>
    }
}
