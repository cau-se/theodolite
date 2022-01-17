package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.util.LoadDimension
import theodolite.util.Resources

/**
 *  Strategy that combines a SearchStrategy and a set of RestrictionStrategy.
 *
 * @param searchStrategy the [SearchStrategy] that is executed as part of this [RestrictionSearch].
 * @param restrictionStrategies the set of [RestrictionStrategy] that are connected conjunctive to restrict the [Resources]
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
@RegisterForReflection
class RestrictionSearch(
    benchmarkExecutor: BenchmarkExecutor,
    private val searchStrategy: SearchStrategy,
    val restrictionStrategies: Set<RestrictionStrategy>
) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Int>): Int? {
        var restrictedResources = resources
        for (strategy in this.restrictionStrategies) {
            restrictedResources = restrictedResources.intersect(strategy.apply(load, resources)).toList()
        }
        return this.searchStrategy.findSuitableResource(load, restrictedResources)
    }

    //TODO: not sure if it makes sense but actually doing the same as for finding suitable resource with the restrictions
    override fun findSuitableLoad(resource: Int, loads: List<LoadDimension>): LoadDimension? {
        //erste Zeile komisch, wird auch bei resource so gemacht aber warum? das ist doch ne liste warum also toList?
        TODO("Not yet implemented")
    }
}