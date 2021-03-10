package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.TopicManager
import java.util.*

class KubernetesBenchmarkDeployment(
    val resources: List<KubernetesResource>,
    private val kafkaConfig: HashMap<String, Any>,
    private val topics: Collection<NewTopic>
) : BenchmarkDeployment {
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(DefaultKubernetesClient().inNamespace("theodolite-she"))

    override fun setup() {
        kafkaController.createTopics(this.topics)
        resources.forEach {
            kubernetesManager.deploy(it)
        }
    }

    override fun teardown() {
        kafkaController.removeTopics(this.topics.map { topic -> topic.name() })
        resources.forEach {
            kubernetesManager.remove(it)
        }
    }
}
