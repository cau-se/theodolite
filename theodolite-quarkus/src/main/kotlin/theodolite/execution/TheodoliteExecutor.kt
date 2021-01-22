package theodolite.execution

import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.*

class TheodoliteExecutor() {
    private fun loadConfig(): Config {
        val benchmark: Benchmark = KafkaBenchmark()
        val results: Results = Results()
        val executor: BenchmarkExecutor = KafkaBenchmarkExecutor(benchmark, results)

        val restrictionStrategy = LowerBoundRestriction(results)
        val searchStrategy = LinearSearch(executor, results)

        return Config(
            loads = (0..6).map{ number -> LoadDimension(number) },
            resources = (0..6).map{ number -> Resource(number) },
            compositeStrategy = CompositeStrategy(executor, searchStrategy, restrictionStrategies = setOf(restrictionStrategy), results = results)
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