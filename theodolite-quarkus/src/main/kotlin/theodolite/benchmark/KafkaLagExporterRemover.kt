package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class KafkaLagExporterRemover(private val client: NamespacedKubernetesClient) {

    fun remove(label: String) {
        this.client.pods().withLabel(label).delete()
        logger.info { "Pod with label: $label deleted" }
    }
}
