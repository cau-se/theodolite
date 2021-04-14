package theodolite.execution

import com.google.gson.GsonBuilder
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.patcher.PatcherDefinitionFactory
import theodolite.strategies.StrategyFactory
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.io.PrintWriter
import java.time.Duration

class TheodoliteExecutor(
    private val config: BenchmarkExecution,
    private val kubernetesBenchmark: KubernetesBenchmark
) {
    lateinit var executor: BenchmarkExecutor

    private fun buildConfig(): Config {
        val results = Results()
        val strategyFactory = StrategyFactory()

        val executionDuration = Duration.ofSeconds(config.execution.duration)

        val resourcePatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                config.resources.resourceType,
                this.kubernetesBenchmark.resourceTypes
            )

        val loadDimensionPatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                config.load.loadType,
                this.kubernetesBenchmark.loadTypes
            )

        executor =
            BenchmarkExecutorImpl(
                benchmark = kubernetesBenchmark,
                results = results,
                executionDuration = executionDuration,
                configurationOverrides = config.configOverrides,
                slo = config.slos[0],
                executionId = config.executionId
            )

        return Config(
            loads = config.load.loadValues.map { load -> LoadDimension(load, loadDimensionPatcherDefinition) },
            resources = config.resources.resourceValues.map { resource ->
                Resource(
                    resource,
                    resourcePatcherDefinition
                )
            },
            compositeStrategy = CompositeStrategy(
                benchmarkExecutor = executor,
                searchStrategy = strategyFactory.createSearchStrategy(executor, config.execution.strategy),
                restrictionStrategies = strategyFactory.createRestrictionStrategy(
                    results,
                    config.execution.restrictions
                )
            )
        )
    }

    fun getExecution(): BenchmarkExecution {
        return this.config
    }

    fun getBenchmark(): KubernetesBenchmark {
        return this.kubernetesBenchmark
    }

    fun run() {
        storeAsFile(this.config, "results/${this.config.executionId}-execution-configuration")
        storeAsFile(kubernetesBenchmark, "results/${this.config.executionId}-benchmark-configuration")

        val config = buildConfig()
        // execute benchmarks for each load
        for (load in config.loads) {
            if (executor.run.get()) {
                config.compositeStrategy.findSuitableResource(load, config.resources)
            }
        }
        storeAsFile(config.compositeStrategy.benchmarkExecutor.results, "results/${this.config.executionId}-result")
    }

    private fun <T> storeAsFile(saveObject: T, filename: String) {
        val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()

        PrintWriter(filename).use { pw ->
            pw.println(gson.toJson(saveObject))
        }
    }
}
