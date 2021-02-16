package theodolite.benchmark

import theodolite.execution.BenchmarkExecutor
import theodolite.execution.BenchmarkExecutorImpl
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.SearchStrategy
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.lang.IllegalArgumentException
import java.time.Duration

class TheodoliteBenchmarkExecutor(
    private val benchmarkContext: BenchmarkContext,
    private val kubernetesBenchmark: KubernetesBenchmark)
{
    private fun createSearchStrategy(executor: BenchmarkExecutor): SearchStrategy {
       return when (this.benchmarkContext.execution.strategy) {
           "LinearSearch" -> LinearSearch(executor)
           "BinarySearch" -> BinarySearch(executor)
           else -> throw  IllegalArgumentException("Search Strategy ${this.benchmarkContext.execution.strategy} not found")
       }
    }

    private fun createRestrictionStrategy(results: Results): Set<RestrictionStrategy> {
        var strategies = emptyList<RestrictionStrategy>()
        strategies = this.benchmarkContext.execution.restrictions.map { restriction ->
          when (restriction) {
                "LowerBound" -> LowerBoundRestriction(results)
                else -> throw  IllegalArgumentException("Restriction Strategy ${this.benchmarkContext.execution.restrictions} not found")
            }
        }
        return strategies.toSet()
    }

    private fun buildConfig(): Config{
       val results = Results()
       val executionDuration = Duration.ofSeconds(this.benchmarkContext.execution.duration)
       val executor: BenchmarkExecutor = BenchmarkExecutorImpl(kubernetesBenchmark, results, executionDuration, this.benchmarkContext.configOverrides)

        return Config(
           loads = benchmarkContext.loads.map { number -> LoadDimension(number) },
           resources = benchmarkContext.resources.map { number -> Resource(number) },
           compositeStrategy = CompositeStrategy(
               benchmarkExecutor = executor,
               searchStrategy = createSearchStrategy(executor),
               restrictionStrategies = createRestrictionStrategy(results)
           ),
           executionDuration = executionDuration
       )

    }



    fun run() {
        val config = buildConfig()

        // execute benchmarks for each load
        for (load in config.loads) {
            config.compositeStrategy.findSuitableResource(load, config.resources)
        }

    }
}
