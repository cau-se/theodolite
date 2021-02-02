package theodolite.execution

import theodolite.k8s.UC1Benchmark
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.*
import java.time.Duration

class TheodoliteExecutor() {
    private fun loadConfig(): Config {
        val benchmark: UC1Benchmark = UC1Benchmark(
            UC1Benchmark.UC1BenchmarkConfig(
                zookeeperConnectionString = "my-confluent-cp-zookeeper:2181",
                kafkaIPConnectionString = "my-confluent-cp-kafka:9092",
                schemaRegistryConnectionString = "http://my-confluent-cp-schema-registry:8081",
                kafkaPartition = 40,
                kafkaReplication = 3,
                kafkaTopics = listOf("input", "output"),
                ucDeploymentPath = "",
                ucServicePath = "",
                wgDeploymentPath = ""
        ))
        val results: Results = Results()
        val executionDuration = Duration.ofSeconds(60*5 )
        val executor: BenchmarkExecutor = KafkaBenchmarkExecutor(benchmark, results, executionDuration)

        val restrictionStrategy = LowerBoundRestriction(results)
        val searchStrategy = LinearSearch(executor, results)

        return Config(
            loads = (0..6).map{ number -> LoadDimension(number) },
            resources = (0..6).map{ number -> Resource(number) },
            compositeStrategy = CompositeStrategy(executor, searchStrategy, restrictionStrategies = setOf(restrictionStrategy), results = results),
            executionDuration = executionDuration
        )
    }

    fun run() {
        // read or get benchmark config
        val config = this.loadConfig()

        // execute benchmarks for each load
        for(load in config.loads) {
            config.compositeStrategy.findSuitableResources(load, config.resources)
        }

    }
}