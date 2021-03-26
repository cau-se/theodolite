package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.patcher.PatcherDefinitionFactory
import theodolite.strategies.StrategyFactory
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TheodoliteExecutor(
    private var config: BenchmarkExecution,
    private var kubernetesBenchmark: KubernetesBenchmark
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
                kubernetesBenchmark,
                results,
                executionDuration,
                config.configOverrides,
                config.slos[0]
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
        val config = buildConfig()
        // execute benchmarks for each load
        for (load in config.loads) {
            if (executor.run) {
                config.compositeStrategy.findSuitableResource(load, config.resources)
            }
        }
    }
}
