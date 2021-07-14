package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.util.LoadDimension
import theodolite.util.Resource

/**
 *  Composite strategy that combines a SearchStrategy and a set of RestrictionStrategy.
 *
 * @param searchStrategy the [SearchStrategy] that is executed as part of this [CompositeStrategy].
 * @param restrictionStrategies the set of [RestrictionStrategy] that are connected conjunctive to restrict the [Resource]
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
@RegisterForReflection
class CompositeStrategy(
    benchmarkExecutor: BenchmarkExecutor,
    private val searchStrategy: SearchStrategy,
    val restrictionStrategies: Set<RestrictionStrategy>
) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        var restrictedResources = resources.toList()
        for (strategy in this.restrictionStrategies) {
            restrictedResources = restrictedResources.intersect(strategy.apply(load, resources)).toList()
        }
        return this.searchStrategy.findSuitableResource(load, restrictedResources)
    }
}
