package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

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
    fun removePod(label: String) {
        this.client.pods().withLabel(label).delete()
        logger.info { "Pod with label: $label deleted" }
    }
}
