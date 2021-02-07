package theodolite.execution

import mu.KotlinLogging
import theodolite.k8s.UC1Benchmark
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.nio.file.Paths
import java.time.Duration

private val logger = KotlinLogging.logger {}

class TheodoliteExecutor() {
    val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
    val resourcesPath = Paths.get(projectDirAbsolutePath, "./../../../resources/main/yaml/")
    private fun loadConfig(): Config {
        logger.info { resourcesPath }
        val benchmark: UC1Benchmark = UC1Benchmark(
            UC1Benchmark.UC1BenchmarkConfig(    // use port forward 2181 -> 2181
                zookeeperConnectionString = "localhost:2181", //"my-confluent-cp-zookeeper:2181", //localhost:2181.
                kafkaIPConnectionString = "localhost:9092",//"my-confluent-cp-kafka:","178.18.0."
                schemaRegistryConnectionString = "http://my-confluent-cp-schema-registry:8081",
                kafkaPartition = 40,
                kafkaReplication = 1,
                kafkaTopics = listOf("input", "output"),
                // TODO("handle path in a more nice way (not absolut)")
                ucDeploymentPath = "$resourcesPath/aggregation-deployment.yaml",
                ucServicePath = "$resourcesPath/aggregation-service.yaml",
                wgDeploymentPath = "$resourcesPath/workloadGenerator.yaml",
                configMapPath = "$resourcesPath/jmx-configmap.yaml",
                ucImageURL = "ghcr.io/cau-se/theodolite-uc1-kstreams-app:latest",
                wgImageURL = "ghcr.io/cau-se/theodolite-uc1-workload-generator:theodolite-kotlin-latest"
            )
        )
        val results: Results = Results()

        val executionDuration = Duration.ofSeconds(60 * 5)

        val executor: BenchmarkExecutor = BenchmarkExecutorImpl(benchmark, results, executionDuration)

        val restrictionStrategy = LowerBoundRestriction(results)
        val searchStrategy = LinearSearch(executor)

        return Config(
            loads = listOf(5000, 10000).map { number -> LoadDimension(number) },
            resources = (1..6).map { number -> Resource(number) },
            compositeStrategy = CompositeStrategy(
                executor,
                searchStrategy,
                restrictionStrategies = setOf(restrictionStrategy)
            ),
            executionDuration = executionDuration
        )
    }

    fun run() {
        // read or get benchmark config
        val config = this.loadConfig()

        // execute benchmarks for each load
        for (load in config.loads) {
            config.compositeStrategy.findSuitableResource(load, config.resources)
        }
    }
}
