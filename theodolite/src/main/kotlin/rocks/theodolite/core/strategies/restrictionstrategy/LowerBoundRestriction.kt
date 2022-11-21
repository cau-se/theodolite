package rocks.theodolite.core.strategies.restrictionstrategy

import rocks.theodolite.core.Results

/**
 * The [LowerBoundRestriction] sets the lower bound of the resources to be examined in the experiment to the value
 * needed to successfully execute the previous smaller load (demand metric), or sets the lower bound of the loads
 * to be examined in the experiment to the largest value, which still successfully executed the previous smaller
 * resource (capacity metric).
 *
 * @param results [Result] object used as a basis to restrict the resources.
 */
class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {

    override fun apply(xValue: Int, yValues: List<Int>): List<Int> {
        val maxXValue = this.results.getMaxBenchmarkedXDimensionValue(xValue)
        // Get previous largest y value or restrict to empty list
        val lowerBound: Int = this.results.getOptYDimensionValue(maxXValue) ?: return listOf()
        return yValues.filter { x -> x >= lowerBound }
    }

}
