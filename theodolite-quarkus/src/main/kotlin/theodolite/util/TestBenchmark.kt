package theodolite.util

import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkDeployment

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
), Benchmark  {

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

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        override: Map<String, String>
    ): BenchmarkDeployment {
        TODO("Not yet implemented")
    }
}
