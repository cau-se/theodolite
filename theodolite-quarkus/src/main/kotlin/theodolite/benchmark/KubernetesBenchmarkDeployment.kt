package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.TopicManager

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
    val resources: List<KubernetesResource>,
    private val kafkaConfig: HashMap<String, Any>,
    private val topics: Collection<NewTopic>
) : BenchmarkDeployment {
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(DefaultKubernetesClient().inNamespace(namespace))

    //label of the KafkaLagExporter.
    private val LABEL = "app.kubernetes.io/name=kafka-lag-exporter"
    private val client = DefaultKubernetesClient().inNamespace(namespace)

    /**
     * Setup a [KubernetesBenchmark] using the [TopicManager] and the [K8sManager]:
     *  - Create the needed topics.
     *  - Deploy the needed resources.
     */
    override fun setup() {
        kafkaController.createTopics(this.topics)
        resources.forEach {
            kubernetesManager.deploy(it)
        }
    }

    /**
     * Tears a [KubernetesBenchmark] down:
     *  - Reset the Kafka Lag Exporter.
     *  - Remove the used topics.
     *  - Remove the [KubernetesResource]s.
     */
    override fun teardown() {
        KafkaLagExporterRemover(client).remove(LABEL)
        kafkaController.removeTopics(this.topics.map { topic -> topic.name() })
        resources.forEach {
            kubernetesManager.remove(it)
        }
    }
}
