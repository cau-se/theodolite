package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Central class that saves the state of an execution of Theodolite. For an execution, it is used to save the result of
 * individual experiments. Further, it is used by the RestrictionStrategy to
 * perform the [theodolite.strategies.restriction.RestrictionStrategy].
 */
@RegisterForReflection
class Results {
    private val results: MutableMap<Pair<LoadDimension, Int>, Boolean> = mutableMapOf()

    /**
     * Set the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the [LoadDimension] and [Resources].
     * @param successful the result of the experiment. Successful == true and Unsuccessful == false.
     */
    fun setResult(experiment: Pair<LoadDimension, Int>, successful: Boolean) {
        this.results[experiment] = successful
    }

    /**
     * Get the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the [LoadDimension] and [Resources].
     * @return true if the experiment was successful and false otherwise. If the result has not been reported so far,
     * null is returned.
     *
     * @see Resources
     */
    fun getResult(experiment: Pair<LoadDimension, Int>): Boolean? {
        return this.results[experiment]
    }

    /**
     * Get the smallest suitable number of instances for a specified [LoadDimension].
     *
     * @param load the [LoadDimension]
     *
     * @return the smallest suitable number of resources. If the experiment was not executed yet,
     * a @see Resource with the constant Int.MAX_VALUE as value is returned.
     * If no experiments have been marked as either successful or unsuccessful
     * yet, a Resource with the constant value Int.MIN_VALUE is returned.
     */
    fun getMinRequiredInstances(load: LoadDimension?): Int {
        if (this.results.isEmpty()) {
            return Int.MIN_VALUE
        }

        var minRequiredInstances = Int.MAX_VALUE
        for (experiment in results) {
            // Get all successful experiments for requested load
            if (experiment.key.first == load && experiment.value) {
                if (experiment.key.second < minRequiredInstances) {
                    // Found new smallest resources
                    minRequiredInstances = experiment.key.second
                }
            }
        }
        return minRequiredInstances
    }

    /**
     * Get the largest [LoadDimension] that has been reported executed successfully (or unsuccessfully) so far, for a
     * [LoadDimension] and is smaller than the given [LoadDimension].
     *
     * @param load the [LoadDimension]
     *
     * @return the largest [LoadDimension] or null, if there is none for this [LoadDimension]
     */
    fun getMaxBenchmarkedLoad(load: LoadDimension): LoadDimension? {
        var maxBenchmarkedLoad: LoadDimension? = null
        for (experiment in results) {
            if (experiment.key.first.get() <= load.get()) {
                if (maxBenchmarkedLoad == null) {
                    maxBenchmarkedLoad = experiment.key.first
                } else if (maxBenchmarkedLoad.get() < experiment.key.first.get()) {
                    maxBenchmarkedLoad = experiment.key.first
                }
            }
        }
        return maxBenchmarkedLoad
    }

    /**
     * Checks whether the results are empty.
     *
     * @return true if [results] is empty.
     */
    fun isEmpty(): Boolean{
        return results.isEmpty()
    }
}
