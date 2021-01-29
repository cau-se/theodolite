package theodolite.k8s

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import org.apache.kafka.common.internals.Topic
import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.k8s.WorkloadGeneratorStateCleaner
import java.io.FileNotFoundException

class UC1Benchmark(config: UC1BenchmarkConfig) : Benchmark(config) {
    val workloadGeneratorStateCleaner: WorkloadGeneratorStateCleaner
    val topicManager: TopicManager
    // TODO("service monitor")
    val kubernetesClient: NamespacedKubernetesClient
    val yamlLoader: YamlLoader
    val deploymentManager: DeploymentManager
    val serviceManager: ServiceManager
    var ucDeployment: Deployment
    var ucService: Service


    init {
        this.workloadGeneratorStateCleaner = WorkloadGeneratorStateCleaner(this.config.zookeeperConnectionString)
        this.topicManager = TopicManager(this.config.kafkaIPConnectionString)
        this.kubernetesClient = DefaultKubernetesClient()
        this.yamlLoader = YamlLoader(this.kubernetesClient)
        this.deploymentManager = DeploymentManager(this.kubernetesClient)
        this.serviceManager = ServiceManager(this.kubernetesClient)
        ucDeployment = this.yamlLoader.loadDeployment(this.config.ucDeploymentPath)!!
        ucService = this.yamlLoader.loadService(this.config.ucServicePath)!!



    }

    override fun start(load: LoadDimension, resources: Resource) {
        // TODO("extract code to dedicated functions. And, we should create a abstration layer to create the benchmark core, which are identically by all benchmarks")
        this.workloadGeneratorStateCleaner.deleteAll()
        this.topicManager.deleteTopics(this.config.kafkaTopics)
        this.topicManager.createTopics(this.config.kafkaTopics, this.config.kafkaPartition, this.config.kafkaReplication)

        // set environment variables
        val environmentVariables: MutableMap<String, String> = mutableMapOf()
        environmentVariables.put("KAFKA_BOOTSTRAP_SERVER", this.config.kafkaIPConnectionString)
        // environmentVariables.put("replicas", this.config.deploymentReplicas) TODO("add possibility to set replicas")
       environmentVariables.put("SCHEMA_REGISTRY_URL", this.config.schemaRegistryConnectionString)
        this.deploymentManager.setWorkloadEnv(ucDeployment,"uc-application", environmentVariables)

        // create kubernetes resources
        this.deploymentManager.deploy(ucDeployment)
        this.serviceManager.deploy(ucService)

        this.startWorkloadGenerator("uc1", load, "uc1")

    }

    override fun stop() {
        this.workloadGeneratorStateCleaner.deleteAll()
        this.topicManager.deleteTopics(this.config.kafkaTopics)
        this.deploymentManager.delete(this.ucDeployment)
        this.serviceManager.delete(this.ucService)
    }

    override fun startWorkloadGenerator(wg: String, load: LoadDimension, ucId: String) {
        val wgDeployment = this.yamlLoader.loadDeployment(this.config.wgDeploymentPath)
        val environmentVariables: MutableMap<String, String> = mutableMapOf()
        environmentVariables.put("NUM_SENSORS", load.get().toString())
        // TODO ("calculate number of required instances")
        val requiredInstances: Int = 1
        environmentVariables.put("NUM_INSTANCES", requiredInstances.toString())
        wgDeployment?.let { this.deploymentManager.setWorkloadEnv(it, "workload-generator", environmentVariables) }
    }

    data class UC1BenchmarkConfig(
        val zookeeperConnectionString: String,
        val kafkaIPConnectionString: String,
        val schemaRegistryConnectionString: String,
        val kafkaTopics: List<String>,
        val kafkaReplication: Short,
        val kafkaPartition: Int,
        val ucDeploymentPath: String,
        val ucDeploymentReplicas: String,
        val ucServicePath: String,
        val wgDeploymentPath: String
        ) {}
}