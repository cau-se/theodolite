package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.TopicManager
import theodolite.util.KafkaConfig
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Organizes the deployment of benchmarks in Kubernetes.
 *
 * @param namespace to operate in.
 * @param resources List of [KubernetesResource] that are managed.
 * @param kafkaConfig for the organization of Kafka topics.
 * @param topics List of topics that are created or deleted.
 */
@RegisterForReflection
class KubernetesBenchmarkDeployment(
    val namespace: String,
    val appResources: List<KubernetesResource>,
    val loadGenResources: List<KubernetesResource>,
    private val loadGenerationDelay: Long,
    private val afterTeardownDelay: Long,
    private val kafkaConfig: HashMap<String, Any>,
    private val topics: List<KafkaConfig.TopicWrapper>,
    private val client: NamespacedKubernetesClient
) : BenchmarkDeployment {
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(client)
    private val LAG_EXPORTER_POD_LABEL = "app.kubernetes.io/name=kafka-lag-exporter"

    /**
     * Setup a [KubernetesBenchmark] using the [TopicManager] and the [K8sManager]:
     *  - Create the needed topics.
     *  - Deploy the needed resources.
     */
    override fun setup() {
        val kafkaTopics = this.topics.filter { !it.removeOnly }
            .map { NewTopic(it.name, it.numPartitions, it.replicationFactor) }
        kafkaController.createTopics(kafkaTopics)
        appResources.forEach { kubernetesManager.deploy(it) }
        logger.info { "Wait ${this.loadGenerationDelay} seconds before starting the load generator." }
        Thread.sleep(Duration.ofSeconds(this.loadGenerationDelay).toMillis())
        loadGenResources.forEach { kubernetesManager.deploy(it) }
    }

    /**
     * Tears down a [KubernetesBenchmark]:
     *  - Reset the Kafka Lag Exporter.
     *  - Remove the used topics.
     *  - Remove the [KubernetesResource]s.
     */
    override fun teardown() {
        loadGenResources.forEach { kubernetesManager.remove(it) }
        appResources.forEach { kubernetesManager.remove(it) }
        kafkaController.removeTopics(this.topics.map { topic -> topic.name })
        KafkaLagExporterRemover(client).remove(LAG_EXPORTER_POD_LABEL)
        logger.info { "Teardown complete. Wait $afterTeardownDelay ms to let everything come down." }
        Thread.sleep(Duration.ofSeconds(afterTeardownDelay).toMillis())
    }
}
