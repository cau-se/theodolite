package theodolite.execution

import theodolite.k8s.UC1Benchmark
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TheodoliteExecutor() {
    val path = "/home/lorenz/git/spesb/theodolite-quarkus/src/main/resources/yaml"
    private fun loadConfig(): Config {
        val benchmark: UC1Benchmark = UC1Benchmark(
            UC1Benchmark.UC1BenchmarkConfig(    // use port forward 2181 -> 2181
                zookeeperConnectionString = "127.0.0.1:2181", //"my-confluent-cp-zookeeper:2181", //localhost:2181.
                kafkaIPConnectionString = "localhost:9093",//"my-confluent-cp-kafka:","178.18.0."
                schemaRegistryConnectionString = "http://my-confluent-cp-schema-registry:8081",
                kafkaPartition = 40,
                kafkaReplication = 1,
                kafkaTopics = listOf("input", "output"),
                // TODO("handle path in a more nice way (not absolut)")
                ucDeploymentPath = path + "/aggregation-deployment.yaml",
                ucServicePath = path + "/aggregation-service.yaml",
                wgDeploymentPath = path + "/workloadGenerator.yaml",
                ucImageURL = "ghcr.io/cau-se/theodolite-uc1-kstreams-app:latest",
                wgImageURL = "ghcr.io/cau-se/theodolite-uc1-kstreams-workload-generator:latest"
            )
        )
        val results: Results = Results()
        val executionDuration = Duration.ofSeconds(60 * 5)
        val executor: BenchmarkExecutor = KafkaBenchmarkExecutor(benchmark, results, executionDuration)

        val restrictionStrategy = LowerBoundRestriction(results)
        val searchStrategy = LinearSearch(executor, results)

        return Config(
            loads = (0..6).map { number -> LoadDimension(number) },
            resources = (0..6).map { number -> Resource(number) },
            compositeStrategy = CompositeStrategy(
                executor,
                searchStrategy,
                restrictionStrategies = setOf(restrictionStrategy),
                results = results
            ),
            executionDuration = executionDuration
        )
    }

    fun run() {
        // read or get benchmark config
        val config = this.loadConfig()

        // execute benchmarks for each load
        for (load in config.loads) {
            config.compositeStrategy.findSuitableResources(load, config.resources)
        }

    }
}