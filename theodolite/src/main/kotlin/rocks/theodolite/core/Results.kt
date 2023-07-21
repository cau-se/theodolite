package rocks.theodolite.core

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.core.strategies.Metric

/**
 * Central class that saves the state of an execution of Theodolite. For an execution, it is used to save the result of
 * individual experiments. Further, it is used by the RestrictionStrategy to
 * perform the [theodolite.strategies.restriction.RestrictionStrategy].
 */
@RegisterForReflection
class Results(val metric: Metric) {
    // (load, resource) -> Boolean map
    private val experimentResults: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf()

    // if metric is "demand"  : load     -> resource
    // if metric is "capacity": resource -> load
    private var optimalYValues: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Set the result for an experiment and update the [optimalYValues] list accordingly.
     *
     * @param experiment A pair that identifies the experiment by the Load and Resource.
     * @param successful the result of the experiment. Successful == true and Unsuccessful == false.
     */
    fun addExperimentResult(experiment: Pair<Int, Int>, successful: Boolean) {
        this.experimentResults[experiment] = successful

        when (metric) {
            Metric.DEMAND ->
                if (successful) {
                    if (optimalYValues.containsKey(experiment.first)) {
                        if (optimalYValues[experiment.first]!! > experiment.second) {
                            optimalYValues[experiment.first] = experiment.second
                        }
                    } else {
                        optimalYValues[experiment.first] = experiment.second
                    }
                } else if (!optimalYValues.containsKey(experiment.first)) {
                    optimalYValues[experiment.first] = Int.MAX_VALUE
                }

            Metric.CAPACITY ->
                if (successful) {
                    if (optimalYValues.containsKey(experiment.second)) {
                        if (optimalYValues[experiment.second]!! < experiment.first) {
                            optimalYValues[experiment.second] = experiment.first
                        }
                    } else {
                        optimalYValues[experiment.second] = experiment.first
                    }
                } else if (!optimalYValues.containsKey(experiment.second)) {
                    optimalYValues[experiment.second] = Int.MIN_VALUE
                }
        }
    }

    /**
     * Get the result for an experiment.
     *
     * @param load Load that identifies the experiment.
     * @param resources Resource that identify the experiment.
     * @return true if the experiment was successful and false otherwise. If the result has not been reported so far,
     * null is returned.
     *
     */
    fun getExperimentResult(load: Int, resources: Int): Boolean? {
        return this.experimentResults[Pair(load, resources)]
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
    fun getOptimalYValue(xValue: Int?): Int? {
        if (xValue != null) {
            val res = optimalYValues[xValue]
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
    fun getPreviousXValue(xValue: Int): Int? {
        return optimalYValues.map { it.key }
            .filter { it <= xValue }
            .maxOrNull()
    }

    /**
     * Checks whether the results are empty.
     *
     * @return true if [experimentResults] is empty.
     */
    fun isEmpty(): Boolean {
        return experimentResults.isEmpty()
    }
}
