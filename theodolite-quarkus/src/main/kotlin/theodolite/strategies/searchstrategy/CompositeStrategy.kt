package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

class CompositeStrategy(benchmarkExecutor: BenchmarkExecutor, val searchStrategy: SearchStrategy, val restrictionStrategies: Set<RestrictionStrategy>, results: Results) : SearchStrategy(benchmarkExecutor, results) {

    override fun findSuitableResources(load: LoadDimension, resources: List<Resource>): Resource? {
        var restrictedResources = resources.toList()
        for (strategy in this.restrictionStrategies) {
            restrictedResources = restrictedResources.intersect(strategy.next(load, resources)).toList()
        }
        return this.searchStrategy.findSuitableResources(load, restrictedResources)
    }
}