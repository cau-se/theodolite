package theodolite.k8s

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource

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
        this.kubernetesClient = DefaultKubernetesClient().inNamespace("default")
        this.yamlLoader = YamlLoader(this.kubernetesClient)
        this.deploymentManager = DeploymentManager(this.kubernetesClient)
        this.serviceManager = ServiceManager(this.kubernetesClient)
        ucDeployment = this.yamlLoader.loadDeployment(this.config.ucDeploymentPath)
        ucService = this.yamlLoader.loadService(this.config.ucServicePath)
    }

    override fun clearClusterEnvironment() {
        this.workloadGeneratorStateCleaner.deleteAll()
        this.topicManager.deleteTopics(this.config.kafkaTopics)
        this.deploymentManager.delete(this.ucDeployment)
        this.serviceManager.delete(this.ucService)
    }

    override fun initializeClusterEnvironment() {
        // this.workloadGeneratorStateCleaner.deleteAll()
        // since the workloadGenerators are not started they cant be deleted

        this.topicManager.deleteTopics(this.config.kafkaTopics)
        this.topicManager.createTopics(
            this.config.kafkaTopics,
            this.config.kafkaPartition,
            this.config.kafkaReplication
        )
    }

    override fun startSUT(resources: Resource) {
        this.deploymentManager.setImageName(ucDeployment, "uc-application", this.config.ucImageURL)

        // set environment variables
        val environmentVariables: MutableMap<String, String> = mutableMapOf()
        environmentVariables.put("KAFKA_BOOTSTRAP_SERVERS", this.config.kafkaIPConnectionString)
        environmentVariables.put("SCHEMA_REGISTRY_URL", this.config.schemaRegistryConnectionString)


        // setup deployment
        this.deploymentManager.setReplica(ucDeployment, resources.get())
        this.deploymentManager.setWorkloadEnv(ucDeployment, "uc-application", environmentVariables)

        // create kubernetes resources
        this.deploymentManager.deploy(ucDeployment)
        this.serviceManager.deploy(ucService)
    }

    override fun startWorkloadGenerator(load: LoadDimension) {
        this.deploymentManager.setImageName(ucDeployment, "workload-generator", this.config.wgImageURL)
        val wgDeployment = this.yamlLoader.loadDeployment(this.config.wgDeploymentPath)

        // TODO ("calculate number of required instances")
        val requiredInstances: Int = 1
        val environmentVariables: MutableMap<String, String> = mutableMapOf()
        environmentVariables.put("KAFKA_BOOTSTRAP_SERVERS", this.config.kafkaIPConnectionString)
        environmentVariables.put("ZK_HOST", this.config.schemaRegistryConnectionString.split(":")[0])
        environmentVariables.put("ZK_PORT", this.config.schemaRegistryConnectionString.split(":")[1])


        environmentVariables.put("NUM_SENSORS", load.get().toString())
        environmentVariables.put("NUM_INSTANCES", requiredInstances.toString())

        this.deploymentManager.setWorkloadEnv(wgDeployment, "workload-generator", environmentVariables)
    }

    data class UC1BenchmarkConfig(
        val zookeeperConnectionString: String,
        val kafkaIPConnectionString: String,
        val schemaRegistryConnectionString: String,
        val kafkaTopics: List<String>,
        val kafkaReplication: Short,
        val kafkaPartition: Int,
        val ucDeploymentPath: String,
        val ucServicePath: String,
        val wgDeploymentPath: String,
        val ucImageURL: String,
        val wgImageURL: String
    ) {}
}