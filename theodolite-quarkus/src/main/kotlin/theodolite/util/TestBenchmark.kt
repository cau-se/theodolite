package theodolite.util

import theodolite.k8s.UC1Benchmark

class TestBenchmark: Benchmark(UC1Benchmark.UC1BenchmarkConfig(
    zookeeperConnectionString = "",
    kafkaIPConnectionString = "",
    schemaRegistryConnectionString = "",
    kafkaTopics = emptyList(),
    kafkaReplication = 0,
    kafkaPartition  = 0,
    ucServicePath = "",
    ucDeploymentPath = "",
    wgDeploymentPath = "",
    ucImageURL = "",
    wgImageURL = ""
)){

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