package theodolite.evaluation

import theodolite.util.PrometheusResponse

/**
 * A SloChecker can be used to evaluate data from Prometheus.
 * @constructor Creates an empty SloChecker
 */
interface SloChecker {
    /**
     * Evaluates [fetchedData] and returns if the experiment was successful.
     * Returns if the evaluated experiment was successful.
     *
     * @param start of the experiment
     * @param end of the experiment
     * @param fetchedData from Prometheus that will be evaluated.
     * @return true if experiment was successful. Otherwise false.
     */
    fun evaluate(fetchedData: List<PrometheusResponse>): Boolean
}
