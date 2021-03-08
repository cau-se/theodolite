package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.util.LoadDimension
import theodolite.util.Resource

class CompositeStrategy(
    benchmarkExecutor: BenchmarkExecutor,
    private val searchStrategy: SearchStrategy,
    val restrictionStrategies: Set<RestrictionStrategy>
) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        var restrictedResources = resources.toList()
        for (strategy in this.restrictionStrategies) {
            restrictedResources = restrictedResources.intersect(strategy.next(load, resources)).toList()
        }
        return this.searchStrategy.findSuitableResource(load, restrictedResources)
    }
}
