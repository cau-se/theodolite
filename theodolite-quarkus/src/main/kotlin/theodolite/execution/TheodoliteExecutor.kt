package theodolite.execution

import theodolite.benchmark.BenchmarkContext
import theodolite.benchmark.KubernetesBenchmark
import theodolite.strategies.StrategiesManager
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TheodoliteExecutor(
    private val config: BenchmarkContext,
    private val kubernetesBenchmark: KubernetesBenchmark
)
{

    private fun buildConfig(): Config{
        val results = Results()
        val strategyManager = StrategiesManager()

        val executionDuration = Duration.ofSeconds(config.execution.duration)
        val executor = BenchmarkExecutorImpl(kubernetesBenchmark, results, executionDuration, config.configOverrides)

        return Config(
           loads = config.load.loadValues.map { load -> LoadDimension(load,  config.load.loadType ) },
           resources = config.resources.resourceValues.map { resource -> Resource(resource, config.load.loadType) },
           compositeStrategy = CompositeStrategy(
               benchmarkExecutor = executor,
               searchStrategy = strategyManager.createSearchStrategy(executor, config.execution.strategy),
               restrictionStrategies = strategyManager.createRestrictionStrategy(results, config.execution.restrictions)),
           executionDuration = executionDuration)
    }



    fun run() {
        val config = buildConfig()

        // execute benchmarks for each load
        for (load in config.loads) {
            config.compositeStrategy.findSuitableResource(load, config.resources)
        }

    }
}
