package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import theodolite.util.AbstractBenchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

class UC1Benchmark(config: AbstractBenchmarkConfig) : AbstractBenchmark(config) {
    private val workloadGeneratorStateCleaner: WorkloadGeneratorStateCleaner
    private val topicManager: TopicManager

    // TODO("service monitor")
    private val kubernetesClient: NamespacedKubernetesClient
    private val yamlLoader: YamlLoader
    private val deploymentManager: DeploymentManager
    private val serviceManager: ServiceManager
    private val configMapManager: ConfigMapManager
    private var ucDeployment: Deployment
    private var ucService: Service
    private var wgDeployment: Deployment
    private var configMap: ConfigMap

    init {
        this.workloadGeneratorStateCleaner =
            WorkloadGeneratorStateCleaner(this.config.zookeeperConnectionString, path = "/workload-generation")
        this.topicManager = TopicManager(this.config.kafkaIPConnectionString)
        this.kubernetesClient = DefaultKubernetesClient().inNamespace("default")
        this.yamlLoader = YamlLoader(this.kubernetesClient)
        this.deploymentManager = DeploymentManager(this.kubernetesClient)
        this.serviceManager = ServiceManager(this.kubernetesClient)
        this.configMapManager = ConfigMapManager(this.kubernetesClient)
        ucDeployment = this.yamlLoader.loadDeployment(this.config.ucDeploymentPath)
        ucService = this.yamlLoader.loadService(this.config.ucServicePath)
        wgDeployment = this.yamlLoader.loadDeployment(this.config.wgDeploymentPath)
        configMap = this.yamlLoader.loadConfigmap(this.config.configMapPath)
    }

    override fun clearClusterEnvironment() {
        this.workloadGeneratorStateCleaner.deleteAll()
        this.topicManager.deleteTopics(this.config.kafkaTopics)
        this.deploymentManager.delete(this.ucDeployment)
        this.serviceManager.delete(this.ucService)
        this.deploymentManager.delete(this.wgDeployment)
    }

    override fun initializeClusterEnvironment() {
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
        //environmentVariables.put("KAFKA_BOOTSTRAP_SERVERS", this.config.kafkaIPConnectionString)
        //environmentVariables.put("SCHEMA_REGISTRY_URL", this.config.schemaRegistryConnectionString)


        // setup deployment
        this.deploymentManager.setReplica(ucDeployment, resources.get())
        this.deploymentManager.setWorkloadEnv(ucDeployment, "uc-application", environmentVariables)


        // create kubernetes resources
        this.deploymentManager.deploy(ucDeployment)
        this.serviceManager.deploy(ucService)
        this.configMapManager.deploy(configMap)
    }

    override fun startWorkloadGenerator(load: LoadDimension) {
        this.deploymentManager.setImageName(wgDeployment, "workload-generator", this.config.wgImageURL)


        // TODO ("calculate number of required instances")
        val requiredInstances = 1
        val environmentVariables: MutableMap<String, String> = mutableMapOf()
        //environmentVariables.put("KAFKA_BOOTSTRAP_SERVERS", this.config.kafkaIPConnectionString)
        //environmentVariables.put("ZK_HOST", this.config.zookeeperConnectionString.split(":")[0])
        //environmentVariables.put("ZK_PORT", this.config.zookeeperConnectionString.split(":")[1])
        environmentVariables["NUM_SENSORS"] = load.get().toString()
        environmentVariables["INSTANCES"] = requiredInstances.toString()

        this.deploymentManager.setWorkloadEnv(this.wgDeployment, "workload-generator", environmentVariables)
        this.deploymentManager.deploy(this.wgDeployment)
    }
}
