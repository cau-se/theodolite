package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Central class that saves the state of a execution of Theodolite. For an execution, it is used to save the result of
 * individual experiments. Further, it is used by the RestrictionStrategy to
 * perform the [theodolite.strategies.restriction.RestrictionStrategy].
 */
@RegisterForReflection
class Results {
    private val results: MutableMap<Pair<LoadDimension, Resource>, Boolean> = mutableMapOf()

    /**
     * Set the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the [LoadDimension] and [Resource].
     * @param successful the result of the experiment. Successful == true and Unsuccessful == false.
     */
    fun setResult(experiment: Pair<LoadDimension, Resource>, successful: Boolean) {
        this.results[experiment] = successful
    }

    /**
     * Get the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the [LoadDimension] and [Resource].
     * @return true if the experiment was successful and false otherwise. If the result has not been reported so far,
     * null is returned.
     *
     * @see Resource
     */
    fun getResult(experiment: Pair<LoadDimension, Resource>): Boolean? {
        return this.results[experiment]
    }

    /**
     * Get the smallest suitable number of instances for a specified [LoadDimension].
     *
     * @param load the [LoadDimension]
     *
     * @return the smallest suitable number of resources. If the experiment was not executed yet,
     * a @see Resource with the constant Int.MAX_VALUE as value is returned. If no experiments have been marked as either successful or unsuccessful
     * yet, a Resource with the constant value Int.MIN_VALUE is returned.
     */
    fun getMinRequiredInstances(load: LoadDimension?): Resource? {
        if (this.results.isEmpty()) return Resource(Int.MIN_VALUE, emptyList())

        var requiredInstances: Resource? = Resource(Int.MAX_VALUE, emptyList())
        for (experiment in results) {
            if (experiment.key.first == load && experiment.value) {
                if (requiredInstances == null) {
                    requiredInstances = experiment.key.second
                } else if (experiment.key.second.get() < requiredInstances.get()) {
                    requiredInstances = experiment.key.second
                }
            }
        }
        return requiredInstances
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
            if (experiment.value) {
                if (experiment.key.first.get() <= load.get()) {
                    if (maxBenchmarkedLoad == null) {
                        maxBenchmarkedLoad = experiment.key.first
                    } else if (maxBenchmarkedLoad.get() < experiment.key.first.get()) {
                        maxBenchmarkedLoad = experiment.key.first
                    }
                }
            }
        }
        return maxBenchmarkedLoad
    }
}
