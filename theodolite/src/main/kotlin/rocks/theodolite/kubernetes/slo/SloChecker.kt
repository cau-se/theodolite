package rocks.theodolite.kubernetes.slo

/**
 * A SloChecker can be used to evaluate data from an external metrics source.
 * @constructor Creates an empty SloChecker
 */
interface SloChecker {
    /**
     * Evaluates [fetchedData] and returns if the experiments were successful.
     *
     * @param fetchedData that will be evaluated.
     * @return true if experiments were successful. Otherwise, false.
     */
    fun evaluate(fetchedData: List<MetricQueryResponse>): Boolean
}
