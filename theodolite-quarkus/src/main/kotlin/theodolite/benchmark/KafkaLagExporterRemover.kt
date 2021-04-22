package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Used to reset the KafkaLagExporter by deleting the pod.
 * @param client NamespacedKubernetesClient used for the deletion.
 */
class KafkaLagExporterRemover(private val client: NamespacedKubernetesClient) {

    /**
     * Deletes all pods with the selected label.
     * @param [label] of the pod that should be deleted.
     */
    fun remove(label: String) {
        this.client.pods().withLabel(label).delete()
        logger.info { "Pod with label: $label deleted" }
    }
}
