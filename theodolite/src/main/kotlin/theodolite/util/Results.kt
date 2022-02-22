package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.Metric

/**
 * Central class that saves the state of an execution of Theodolite. For an execution, it is used to save the result of
 * individual experiments. Further, it is used by the RestrictionStrategy to
 * perform the [theodolite.strategies.restriction.RestrictionStrategy].
 */
@RegisterForReflection
class Results (val metric: Metric) {
    // (load, resource) -> Boolean map
    private val results: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()

    // if metric is "demand"  : load     -> resource
    // if metric is "capacity": resource -> load
    private var optInstances: MutableMap<Int, Int> = mutableMapOf()


    /**
     * Set the result for an experiment and update the [optInstances] list accordingly.
     *
     * @param experiment A pair that identifies the experiment by the Load and Resource.
     * @param successful the result of the experiment. Successful == true and Unsuccessful == false.
     */
    fun setResult(experiment: Pair<Int, Int>, successful: Boolean) {
        this.results[experiment] = successful

        when (metric) {
            Metric.DEMAND ->
                if (optInstances.containsKey(experiment.first)) {
                    if(optInstances[experiment.first]!! > experiment.second) {
                        optInstances[experiment.first] = experiment.second
                    }
                } else {
                    if(!successful){
                        optInstances[experiment.first] = Int.MAX_VALUE
                    }else {
                        optInstances[experiment.first] = experiment.second
                    }
                }
            Metric.CAPACITY ->
                if (optInstances.containsKey(experiment.second)) {
                    if (optInstances[experiment.second]!! < experiment.first){
                        optInstances[experiment.second] = experiment.first
                    }
                } else {
                    if(!successful){
                        optInstances[experiment.second] = Int.MIN_VALUE
                    } else {
                        optInstances[experiment.second] = experiment.first
                    }
                }
        }
    }

    /**
     * Get the result for an experiment.
     *
     * @param experiment A pair that identifies the experiment by the Load and Resource.
     * @return true if the experiment was successful and false otherwise. If the result has not been reported so far,
     * null is returned.
     *
     */
    fun getResult(experiment: Pair<Int, Int>): Boolean? {
        return this.results[experiment]
    }

    /**
     * Get the smallest suitable number of instances for a specified x-Value.
     * The x-Value corresponds to the...
     * - load, if the metric is "demand".
     * - resource, if the metric is "capacity".
     *
     * @param xValue the Value of the x-dimension of the current metric
     *
     * @return the smallest suitable number of resources/loads (depending on metric).
     * If there is no experiment that has been executed yet, there is no experiment
     * for the given [xValue] or there is none marked successful yet, null is returned.
     */
    fun getOptYDimensionValue(xValue: Int?): Int? {
        if (xValue != null) {
            val res = optInstances[xValue]
            if (res != Int.MAX_VALUE && res != Int.MIN_VALUE) {
                return res
            }
        }
        return null
    }

    /**
     * Get the largest x-Value that has been tested and reported so far (successful or unsuccessful),
     * which is smaller than the specified x-Value.
     *
     * The x-Value corresponds to the...
     * - load, if the metric is "demand".
     * - resource, if the metric is "capacity".
     *
     * @param xValue the Value of the x-dimension of the current metric
     *
     * @return the largest tested x-Value or null, if there wasn't any tested which is smaller than the [xValue].
     */
    fun getMaxBenchmarkedXDimensionValue(xValue: Int): Int? {
        var maxBenchmarkedXValue: Int? = null
        for (instance in optInstances) {
            val instanceXValue= instance.key
            if (instanceXValue <= xValue) {
                if (maxBenchmarkedXValue == null) {
                    maxBenchmarkedXValue = instanceXValue
                } else if (maxBenchmarkedXValue < instanceXValue) {
                    maxBenchmarkedXValue = instanceXValue
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
}
