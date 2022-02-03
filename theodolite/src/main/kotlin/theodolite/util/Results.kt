package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.Metric

/**
 * Central class that saves the state of an execution of Theodolite. For an execution, it is used to save the result of
 * individual experiments. Further, it is used by the RestrictionStrategy to
 * perform the [theodolite.strategies.restriction.RestrictionStrategy].
 */
@RegisterForReflection
//TODO: Initializing überall anpassen
class Results (val metric: Metric) {
    //TODO: enum statt Boolean
    private val results: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()

    /**
     * Set the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the LoadDimension and Resource.
     * @param successful the result of the experiment. Successful == true and Unsuccessful == false.
     */
    fun setResult(experiment: Pair<Int, Int>, successful: Boolean) {
        this.results[experiment] = successful
    }

    /**
     * Get the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the LoadDimension and Resource.
     * @return true if the experiment was successful and false otherwise. If the result has not been reported so far,
     * null is returned.
     *
     */
    fun getResult(experiment: Pair<Int, Int>): Boolean? {
        return this.results[experiment]
    }

    /**
     * Get the smallest suitable number of instances for a specified LoadDimension.
     *
     * @param load the LoadDimension
     *
     * @return the smallest suitable number of resources. If the experiment was not executed yet,
     * a @see Resource with the constant Int.MAX_VALUE as value is returned.
     * If no experiments have been marked as either successful or unsuccessful
     * yet, a Resource with the constant value Int.MIN_VALUE is returned.
     */
    fun getMinRequiredYDimensionValue(xValue: Int?): Int {
        if (this.results.isEmpty()) { //should add || xValue == null
            return Int.MIN_VALUE
        }

        var minRequiredYValue = Int.MAX_VALUE
        for (experiment in results) {
            // Get all successful experiments for requested load
            if (getXDimensionValue(experiment.key) == xValue && experiment.value) {
                val experimentYValue = getYDimensionValue(experiment.key)
                if (experimentYValue < minRequiredYValue) {
                    // Found new smallest resources
                    minRequiredYValue = experimentYValue
                }
            }
        }
        return minRequiredYValue
    }

    // TODO: SÖREN FRAGEN WARUM WIR DAS BRAUCHEN UND NICHT EINFACH PREV, WEIL NICHT DURCHGELAUFEN?
    // TODO Kommentar zu XDimension und YDimension
    /**
     * Get the largest LoadDimension that has been reported executed successfully (or unsuccessfully) so far, for a
     * LoadDimension and is smaller than the given LoadDimension.
     *
     * @param load the LoadDimension
     *
     * @return the largest LoadDimension or null, if there is none for this LoadDimension
     */
    fun getMaxBenchmarkedXDimensionValue(xValue: Int): Int? {
        var maxBenchmarkedXValue: Int? = null
        for (experiment in results) {
            val experimentXValue = getXDimensionValue(experiment.key)
            if (experimentXValue <= xValue) { //warum \leq?
                if (maxBenchmarkedXValue == null) {
                    maxBenchmarkedXValue = experimentXValue
                } else if (maxBenchmarkedXValue < experimentXValue) {
                    maxBenchmarkedXValue = experimentXValue
                }
            }
        }
        return maxBenchmarkedXValue
    }

    /**
     * Checks whether the results are empty.
     *
     * @return true if [results] is empty.
     */
    fun isEmpty(): Boolean{
        return results.isEmpty()
    }

    fun getYDimensionValue(experimentKey: Pair<Int, Int>): Int{
        if(metric.value == "demand"){
            return experimentKey.second
        }
        return experimentKey.first
    }

    fun getXDimensionValue(experimentKey: Pair<Int, Int>): Int{
        if(metric.value == "demand"){
            return experimentKey.first
        }
        return experimentKey.second
    }
}
