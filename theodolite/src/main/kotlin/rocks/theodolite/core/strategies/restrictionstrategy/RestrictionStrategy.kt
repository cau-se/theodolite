package rocks.theodolite.core.strategies.restrictionstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.core.Results

/**
 * A 'Restriction Strategy' restricts a list of resources or loads depending on the metric based on the current
 * results of all previously performed benchmarks.
 *
 * @param results the [Results] object
 */
@RegisterForReflection
abstract class RestrictionStrategy(val results: Results) {
    /**
     * Apply the restriction of the given resource list for the given load based on the results object (demand metric),
     * or apply the restriction of the given load list for the given resource based on the results object (capacity metric).
     *
     * @param xValue The value to be examined in the experiment, can be load (demand metric) or resource (capacity metric).
     * @param yValues List of values to be restricted, can be resources (demand metric) or loads (capacity metric).
     * @return Returns a list containing only elements that have not been filtered out by the
     * restriction (possibly empty).
     */
    abstract fun apply(xValue: Int, yValues: List<Int>): List<Int>
}
