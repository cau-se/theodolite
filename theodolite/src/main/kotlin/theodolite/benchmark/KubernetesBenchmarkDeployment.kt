package theodolite.benchmark

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.ResourceByLabelHandler
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
    private val sutBeforeActions: List<Action>,
    private val sutAfterActions: List<Action>,
    private val loadGenBeforeActions: List<Action>,
    private val loadGenAfterActions: List<Action>,
    val appResources: List<HasMetadata>,
    val loadGenResources: List<HasMetadata>,
    private val loadGenerationDelay: Long,
    private val afterTeardownDelay: Long,
    private val kafkaConfig: Map<String, Any>,
    private val topics: List<KafkaConfig.TopicWrapper>,
    private val client: NamespacedKubernetesClient
) : BenchmarkDeployment {
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(client)
    private val LAG_EXPORTER_POD_LABEL_NAME = "app.kubernetes.io/name"
    private val LAG_EXPORTER_POD_LABEL_VALUE = "kafka-exporter"

    /**
     * Setup a [KubernetesBenchmark] using the [TopicManager] and the [K8sManager]:
     *  - Create the needed topics.
     *  - Deploy the needed resources.
     */
    override fun setup() {
        if (this.topics.isNotEmpty()) {
            val kafkaTopics = this.topics
                .filter { !it.removeOnly }
                .map { NewTopic(it.name, it.numPartitions, it.replicationFactor) }
            kafkaController.createTopics(kafkaTopics)
        }
        sutBeforeActions.forEach { it.exec(client = client) }
        appResources.forEach { kubernetesManager.deploy(it) }
        logger.info { "Wait ${this.loadGenerationDelay} seconds before starting the load generator." }
        Thread.sleep(Duration.ofSeconds(this.loadGenerationDelay).toMillis())
        loadGenBeforeActions.forEach { it.exec(client = client) }
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
        loadGenAfterActions.forEach { it.exec(client = client) }
        appResources.forEach { kubernetesManager.remove(it) }
        sutAfterActions.forEach { it.exec(client = client) }
        if (this.topics.isNotEmpty()) {
            kafkaController.removeTopics(this.topics.map { topic -> topic.name })
        }
        ResourceByLabelHandler(client).removePods(
            labelName = LAG_EXPORTER_POD_LABEL_NAME,
            labelValue = LAG_EXPORTER_POD_LABEL_VALUE
        )
        logger.info { "Teardown complete. Wait $afterTeardownDelay ms to let everything come down." }
        Thread.sleep(Duration.ofSeconds(afterTeardownDelay).toMillis())
    }
}
