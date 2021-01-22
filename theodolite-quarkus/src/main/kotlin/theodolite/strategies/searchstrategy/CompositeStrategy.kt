package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

class CompositeStrategy(benchmarkExecutor: BenchmarkExecutor, val searchStrategy: SearchStrategy, val restrictionStrategies: List<RestrictionStrategy>, results: Results) : SearchStrategy(benchmarkExecutor, results) {

    override fun findSuitableResources(load: LoadDimension,resources: List<Resource>): Resource? {
        var restricted = resources
        for (strategy in this.restrictionStrategies) {
            restricted = restricted.intersect(strategy.next(load, resources)).toList() // erstellt das eine liste oder verändert das die liste?
        }
        return this.searchStrategy.findSuitableResources(load, restricted)
    }
}