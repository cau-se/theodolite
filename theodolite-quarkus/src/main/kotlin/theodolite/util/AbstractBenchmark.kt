package theodolite.util

abstract class AbstractBenchmark(val config: Config): Benchmark {
    override fun start(load: LoadDimension, resources: Resource) {
        this.clearClusterEnvironment()
        this.initializeClusterEnvironment()
        this.startSUT(resources)
        this.startWorkloadGenerator(load)
    }

    data class Config(
        val clusterZookeeperConnectionString: String,
        val clusterKafkaConnectionString: String,
        val externalZookeeperConnectionString: String,
        val externalKafkaConnectionString: String,
        val schemaRegistryConnectionString: String,
        val kafkaTopics: List<String>,
        val kafkaReplication: Short,
        val kafkaPartition: Int,
        val ucDeploymentPath: String,
        val ucServicePath: String,
        val configMapPath: String,
        val wgDeploymentPath: String,
        val ucImageURL: String,
        val wgImageURL: String
    ) {}
}
