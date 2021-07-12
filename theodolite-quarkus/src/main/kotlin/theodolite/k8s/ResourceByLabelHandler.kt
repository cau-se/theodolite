package theodolite.k8s

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import mu.KotlinLogging
import org.json.JSONObject

private val logger = KotlinLogging.logger {}

/**
 * Used to reset the KafkaLagExporter by deleting the pod.
 * @param client NamespacedKubernetesClient used for the deletion.
 */
// TODO(Maybe we can add support to delete arbitrary resources (kinds),
//  then we can use this class also inside the ClusterSetup class instead of the clearByLabel function.)
class ResourceByLabelRemover(private val client: NamespacedKubernetesClient) {

    /**
     * Deletes all pods with the selected label.
     * @param [label] of the pod that should be deleted.
     */
    fun removePods(label: String) {
        this.client.pods().withLabel(label).delete()
        logger.info { "Pod with label: $label deleted" }
    }

    fun removeServices(labelName: String, labelValue: String) {
        this.client
            .services()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    fun removeDeployments(labelName: String, labelValue: String){
        this.client
            .apps()
            .deployments()
            .withLabel("$labelName=$labelValue")
            .delete()

    }
    fun removeStateFulSets(labelName: String, labelValue: String) {
        this.client
            .apps()
            .statefulSets()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    fun removeConfigMaps(labelName: String, labelValue: String){
        this.client
            .configMaps()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    fun removeCR(labelName: String, labelValue: String, context: CustomResourceDefinitionContext) {
        val customResources = JSONObject(
            this.client.customResource(context)
                .list(client.namespace, mapOf(Pair(labelName, labelValue)))
        )
            .getJSONArray("items")

        (0 until customResources.length())
            .map { customResources.getJSONObject(it).getJSONObject("metadata").getString("name") }
            .forEach { this.client.customResource(context).delete(client.namespace, it) }
    }
}
