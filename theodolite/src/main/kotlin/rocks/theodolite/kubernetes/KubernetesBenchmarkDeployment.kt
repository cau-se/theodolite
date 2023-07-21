package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import org.apache.kafka.clients.admin.NewTopic
import rocks.theodolite.kubernetes.kafka.TopicManager
import rocks.theodolite.kubernetes.model.crd.KafkaConfig
import theodolite.benchmark.RolloutManager
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
    private val rolloutMode: Boolean,
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
     *  - Deploy the needed [KubernetesResource]s (deployment order: SUT resources, loadgenerator resources;
     *    No guaranteed order of files inside configmaps).
     */
    override fun setup() {
        val rolloutManager = RolloutManager(rolloutMode, client)
        if (this.topics.isNotEmpty()) {
            val kafkaTopics = this.topics
                .filter { !it.removeOnly }
                .map { NewTopic(it.name, it.numPartitions, it.replicationFactor) }
            kafkaController.createTopics(kafkaTopics)
        }

        sutBeforeActions.forEach { it.exec(client = client) }
        rolloutManager.rollout(appResources)
        logger.info { "Wait ${this.loadGenerationDelay} seconds before starting the load generator." }
        Thread.sleep(Duration.ofSeconds(this.loadGenerationDelay).toMillis())
        loadGenBeforeActions.forEach { it.exec(client = client) }
        rolloutManager.rollout(loadGenResources)
    }

    /**
     * Tears down a [KubernetesBenchmark]:
     *  - Reset the Kafka Lag Exporter.
     *  - Remove the used topics.
     *  - Remove the [KubernetesResource]s (removal order: loadgenerator resources, SUT resources;
     *    No guaranteed order of files inside configmaps).
     */
    override fun teardown() {
        val podCleaner = ResourceByLabelHandler(client)
        loadGenResources.reversed().forEach { kubernetesManager.remove(it, false) }
        loadGenAfterActions.forEach { it.exec(client = client) }
        appResources.reversed().forEach { kubernetesManager.remove(it,false) }
        sutAfterActions.forEach { it.exec(client = client) }
        if (this.topics.isNotEmpty()) {
            kafkaController.removeTopics(this.topics.map { topic -> topic.name })
        }

        listOf(loadGenResources, appResources)
            .forEach {
                if (it is Deployment) {
                    podCleaner.blockUntilPodsDeleted(it.spec.selector.matchLabels)
                } else if (it is StatefulSet) {
                    podCleaner.blockUntilPodsDeleted(it.spec.selector.matchLabels)
                }
            }

        podCleaner.removePods(
            labelName = LAG_EXPORTER_POD_LABEL_NAME,
            labelValue = LAG_EXPORTER_POD_LABEL_VALUE
        )
        logger.info { "Teardown complete. Wait $afterTeardownDelay seconds to let everything cool down." }
        Thread.sleep(Duration.ofSeconds(afterTeardownDelay).toMillis())
    }
}