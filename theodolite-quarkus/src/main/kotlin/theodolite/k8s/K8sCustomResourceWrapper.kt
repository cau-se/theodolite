package theodolite.k8s

import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext

/**
 * Fabric8 handles custom resources as plain HashMaps. These need to be handled differently than normal
 * Kubernetes resources.  The K8sCustomResourceWrapper class provides a wrapper to deploy and delete
 * custom resources in a uniform way.
 *
 * @param map custom resource as plain hashmap
 * @constructor Create empty K8s custom resource wrapper
 */
class K8sCustomResourceWrapper(private val map: Map<String, String>) : CustomResource() {

    /**
     * Deploy a custom resource
     *
     * @param client a namespaced Kubernetes client which are used to deploy the CR object.
     */
    fun deploy(client: NamespacedKubernetesClient) {
        val kind = this.map["kind"]
        // Search the CustomResourceDefinition to which the CR Object belongs.
        // This should be exactly one if the CRD is registered for Kubernetes, zero otherwise.
        val crds = client.apiextensions().v1beta1().customResourceDefinitions().list()
        crds.items
            .filter { crd -> crd.toString().contains("kind=$kind") }
            .map { crd -> CustomResourceDefinitionContext.fromCrd(crd) }
            .forEach { context ->
                client.customResource(context).createOrReplace(
                    client.configuration.namespace, this.map as Map<String, Any>
                )
            }
    }

    /**
     * Delete a custom resource
     *
     * @param client a namespaced Kubernetes client which are used to delete the CR object.
     */
    fun delete(client: NamespacedKubernetesClient) {
        val kind = this.map["kind"]
        val metadata = this.map["metadata"] as HashMap<String, String>
        val name = metadata["name"]

        val crds = client.apiextensions().v1beta1().customResourceDefinitions().list()
        crds.items
            .filter { crd -> crd.toString().contains("kind=$kind") }
            .map { crd -> CustomResourceDefinitionContext.fromCrd(crd) }
            .forEach { context ->
                client.customResource(context).delete(client.configuration.namespace, name)
            }
    }
}
