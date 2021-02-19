package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.apache.kafka.clients.admin.NewTopic
import theodolite.k8s.K8sManager
import theodolite.k8s.TopicManager
import theodolite.k8s.WorkloadGeneratorStateCleaner
import java.util.*

class KubernetesBenchmarkDeployment(
    val resources: List<KubernetesResource>,
    private val kafkaConfig: HashMap<String, Any>,
    private val zookeeperConfig: String,
    private val topics: Collection<NewTopic>
    // Maybe more
): BenchmarkDeployment {
    private val workloadGeneratorStateCleaner = WorkloadGeneratorStateCleaner(this.zookeeperConfig)
    private val kafkaController = TopicManager(this.kafkaConfig)
    private val kubernetesManager = K8sManager(DefaultKubernetesClient().inNamespace("default")) // Maybe per resource type

    override fun setup() {
        this.workloadGeneratorStateCleaner.deleteState()
        kafkaController.createTopics(this.topics)
        resources.forEach {
            kubernetesManager.deploy(it)
        }
    }

    override fun teardown() {
        this.workloadGeneratorStateCleaner.deleteState()
        kafkaController.removeTopics(this.topics.map { topic -> topic.name() })
        resources.forEach {
            kubernetesManager.remove(it)
        }

    }
}
