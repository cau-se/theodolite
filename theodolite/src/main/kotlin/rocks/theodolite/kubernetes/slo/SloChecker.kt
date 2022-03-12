package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.util.PrometheusResponse

/**
 * A SloChecker can be used to evaluate data from Prometheus.
 * @constructor Creates an empty SloChecker
 */
interface SloChecker {
    /**
     * Evaluates [fetchedData] and returns if the experiments were successful.
     *
     * @param fetchedData from Prometheus that will be evaluated.
     * @return true if experiments were successful. Otherwise, false.
     */
    fun evaluate(fetchedData: List<PrometheusResponse>): Boolean
}
