package theodolite.strategies.restriction

import theodolite.util.LoadDimension
import theodolite.util.Results

/**
 * The [LowerBoundRestriction] sets the lower bound of the resources to be examined to the value
 * needed to successfully execute the next smaller load.
 *
 * @param results [Result] object used as a basis to restrict the resources.
 */
class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {

    override fun apply(load: LoadDimension, resources: List<Int>): List<Int> {
        val maxLoad: LoadDimension? = this.results.getMaxBenchmarkedLoad(load)
        var lowerBound: Int? = this.results.getMinRequiredInstances(maxLoad)
        if (lowerBound == null) {
            lowerBound = resources[0]
        }
        return resources.filter { x -> x >= lowerBound }
    }

}
