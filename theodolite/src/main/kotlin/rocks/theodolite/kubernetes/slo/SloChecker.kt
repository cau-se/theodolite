package rocks.theodolite.kubernetes.slo

import rocks.theodolite.core.SloExperimentResult

/**
 * A SloChecker can be used to evaluate data from an external metrics source.
 * @constructor Creates an empty SloChecker
 */
interface SloChecker {
    /**
     * Evaluates [fetchedData] and returns the SLO experiment outcome.
     *
     * @param fetchedData that will be evaluated.
     * @return [SloExperimentResult.SUCCESS] if the SLO passed, [SloExperimentResult.FAILURE] otherwise.
     */
    fun evaluate(fetchedData: List<MetricQueryResponse>): SloExperimentResult
}
