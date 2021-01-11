package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

class CompositeStrategy(benchmarkExecutor: BenchmarkExecutor, val searchStrategy: SearchStrategy, val restrictionStrategies: List<RestrictionStrategy>) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResources(load: LoadDimension,resources: List<Resource>): Resource? {
        var restrictedResources: List<Resource> = resources
        for (strategy in this.restrictionStrategies) {
            restrictedResources.intersect(strategy.next(load, resources))
        }
        return this.searchStrategy.findSuitableResources(load, restrictedResources)
    }
}