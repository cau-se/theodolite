package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import mu.KotlinLogging
import org.json.JSONObject

private val logger = KotlinLogging.logger {}

/**
 * The ResourceByLabelHandler provides basic functions to manage Kubernetes resources through their labels.
 * @param client NamespacedKubernetesClient used for the deletion.
 */
class ResourceByLabelHandler(private val client: NamespacedKubernetesClient) {

    /**
     * Deletes all pods with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
    fun removePods(labelName: String, labelValue: String) {
        this.client
            .pods()
            .withLabel("$labelName=$labelValue").delete()
        logger.info { "Pod with label: $labelName=$labelValue deleted" }
    }

    /**
     * Deletes all services with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
    fun removeServices(labelName: String, labelValue: String) {
        this.client
            .services()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    /**
     * Deletes all deployments with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
    fun removeDeployments(labelName: String, labelValue: String) {
        this.client
            .apps()
            .deployments()
            .withLabel("$labelName=$labelValue")
            .delete()

    }

    /**
     * Deletes all stateful sets with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
    fun removeStatefulSets(labelName: String, labelValue: String) {
        this.client
            .apps()
            .statefulSets()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    /**
     * Deletes all configmaps with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
    fun removeConfigMaps(labelName: String, labelValue: String) {
        this.client
            .configMaps()
            .withLabel("$labelName=$labelValue")
            .delete()
    }

    /**
     * Deletes all custom resources sets with the selected label.
     * @param [labelName] the label name
     * @param [labelValue] the value of this label
     */
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

    /**
     * Block until all pods with are deleted
     *
     * @param matchLabels Map of label keys to label values to be deleted
     * */
    fun blockUntilPodsDeleted(matchLabels: Map<String, String>) {
        while (
            !this.client
                .pods()
                .withLabels(matchLabels)
                .list()
                .items
                .isNullOrEmpty()
        ) {
            logger.info { "Wait for pods with label $matchLabels to be deleted." }
            Thread.sleep(1000)
        }
    }
}
