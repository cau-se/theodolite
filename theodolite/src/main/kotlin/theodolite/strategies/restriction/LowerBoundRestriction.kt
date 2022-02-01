package theodolite.strategies.restriction

import theodolite.util.Results

/**
 * The [LowerBoundRestriction] sets the lower bound of the resources to be examined to the value
 * needed to successfully execute the next smaller load.
 *
 * @param results [Result] object used as a basis to restrict the resources.
 */
class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {

    override fun apply(load: Int, resources: List<Int>): List<Int> {
        val maxLoad: Int? = this.results.getMaxBenchmarkedLoad(load)
        var lowerBound: Int = this.results.getMinRequiredInstances(maxLoad)
        if (lowerBound == Int.MIN_VALUE || lowerBound == Int.MAX_VALUE) {
            lowerBound = resources[0]
        }
        return resources.filter { x -> x >= lowerBound }
    }

}
