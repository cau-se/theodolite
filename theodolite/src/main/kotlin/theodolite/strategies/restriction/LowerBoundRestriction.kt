package theodolite.strategies.restriction

import theodolite.util.Results

/**
 * The [LowerBoundRestriction] sets the lower bound of the resources to be examined to the value
 * needed to successfully execute the next smaller load.
 *
 * @param results [Result] object used as a basis to restrict the resources.
 */
class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {

    override fun apply(xValue: Int, yValues: List<Int>): List<Int> {
        val maxXValue: Int? = this.results.getMaxBenchmarkedXDimensionValue(xValue)
        var lowerBound: Int? = this.results.getOptYDimensionValue(maxXValue)
        if (lowerBound == null) {
            lowerBound = yValues[0]
        }
        return yValues.filter { x -> x >= lowerBound }
    }

}
