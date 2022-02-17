package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy

/**
 *  Strategy that combines a SearchStrategy and a set of RestrictionStrategy.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 * @param searchStrategy the [SearchStrategy] that is executed as part of this [RestrictionSearch].
 * @param restrictionStrategies the set of [RestrictionStrategy] that are connected conjunctive to restrict the Resource.
 *
 */
@RegisterForReflection
class RestrictionSearch(
    benchmarkExecutor: BenchmarkExecutor,
    private val searchStrategy: SearchStrategy,
    private val restrictionStrategies: Set<RestrictionStrategy>
) : SearchStrategy(benchmarkExecutor) {

    /**
     * Restricting the possible resources and calling findSuitableResource of the given [SearchStrategy].
     */
    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {
        var restrictedResources = resources
        for (strategy in this.restrictionStrategies) {
            restrictedResources = restrictedResources.intersect(strategy.apply(load, resources).toSet()).toList()
        }
        return this.searchStrategy.findSuitableResource(load, restrictedResources)
    }

    /**
     * Restricting the possible loads and calling findSuitableLoad of the given [SearchStrategy].
     */
    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {
        var restrictedLoads = loads
        for (strategy in this.restrictionStrategies) {
            restrictedLoads = restrictedLoads.intersect(strategy.apply(resource, loads).toSet()).toList()
        }
        return this.searchStrategy.findSuitableLoad(resource, restrictedLoads)
    }
}