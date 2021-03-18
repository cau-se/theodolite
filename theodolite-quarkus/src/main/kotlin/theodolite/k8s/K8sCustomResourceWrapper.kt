package theodolite.k8s

import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext

class K8sCustomResourceWrapper(private val map : Map<String,String>) : CustomResource() {


    fun deploy(client : NamespacedKubernetesClient){
        val kind = this.map["kind"]
        val crds = client.apiextensions().v1beta1().customResourceDefinitions().list()
        crds.items
            .filter { crd -> crd.toString().contains("kind=$kind") }
            .map { crd -> CustomResourceDefinitionContext.fromCrd(crd) }
            .forEach { context ->
                client.customResource(context).createOrReplace(client.configuration.namespace,this.map as Map<String, Any>)
            }
    }

    fun delete(client : NamespacedKubernetesClient){
        val kind = this.map["kind"]
        val metadata = this.map["metadata"] as HashMap<String,String>
        val name = metadata["name"]

        val crds = client.apiextensions().v1beta1().customResourceDefinitions().list()
        crds.items
            .filter { crd -> crd.toString().contains("kind=$kind") }
            .map { crd -> CustomResourceDefinitionContext.fromCrd(crd)   }
            .forEach { context ->
                client.customResource(context).delete(client.configuration.namespace,name) }
    }

}