package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.TopicManager

@RegisterForReflection
class KubernetesBenchmarkDeployment(
    val namespace: String,
    val resources: List<KubernetesResource>,
    private val kafkaConfig: HashMap<String, Any>,
    private val topics: Collection<NewTopic>,
    private val client: NamespacedKubernetesClient
) : BenchmarkDeployment {
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(client)
    private val LABEL = "app.kubernetes.io/name=kafka-lag-exporter"

    override fun setup() {
        kafkaController.createTopics(this.topics)
        resources.forEach {
            kubernetesManager.deploy(it)
        }
    }

    override fun teardown() {
        KafkaLagExporterRemover(client).remove(LABEL)
        kafkaController.removeTopics(this.topics.map { topic -> topic.name() })
        resources.forEach {
            kubernetesManager.remove(it)
        }
    }
}
