package theodolite.util

class TestBenchmark : AbstractBenchmark(
    AbstractBenchmark.Config(
        clusterZookeeperConnectionString = "",
        clusterKafkaConnectionString = "",
        externalZookeeperConnectionString = "",
        externalKafkaConnectionString = "",
        schemaRegistryConnectionString = "",
        kafkaTopics = emptyList(),
        kafkaReplication = 0,
        kafkaPartition = 0,
        ucServicePath = "",
        ucDeploymentPath = "",
        wgDeploymentPath = "",
        configMapPath = "",
        ucImageURL = "",
        wgImageURL = ""
    )
) {

    override fun initializeClusterEnvironment() {
        TODO("Not yet implemented")
    }

    override fun clearClusterEnvironment() {
        TODO("Not yet implemented")
    }

    override fun startSUT(resources: Resource) {
        TODO("Not yet implemented")
    }

    override fun startWorkloadGenerator(load: LoadDimension) {
        TODO("Not yet implemented")
    }
}
