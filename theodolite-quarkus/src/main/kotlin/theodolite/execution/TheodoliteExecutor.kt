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
    private val benchmarkContext: BenchmarkContext,
    private val kubernetesBenchmark: KubernetesBenchmark
)
{

    private fun buildConfig(): Config{
        val results = Results()
        val strategyManager = StrategiesManager()

        val executionDuration = Duration.ofSeconds(this.benchmarkContext.execution.duration)
        val executor = BenchmarkExecutorImpl(kubernetesBenchmark, results, executionDuration, this.benchmarkContext.configOverrides)

        return Config(
           loads = benchmarkContext.loads.map { number -> LoadDimension(number) },
           resources = benchmarkContext.resources.map { number -> Resource(number) },
           compositeStrategy = CompositeStrategy(
               benchmarkExecutor = executor,
               searchStrategy = strategyManager.createSearchStrategy(executor, this.benchmarkContext.execution.strategy),
               restrictionStrategies = strategyManager.createRestrictionStrategy(results, this.benchmarkContext.execution.restrictions)),
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
