package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

private val logger = KotlinLogging.logger {}

/**
 * Evaluates a set of SLOs against pre-collected SLI data.
 *
 * @param slos List of resolved SLOs to evaluate.
 * @param executionId Used for logging context.
 */
class SloEvaluator(
    private val slos: List<Slo>,
    private val executionId: Int
) {
    /**
     * Evaluates all SLOs against the [collectedSliData] map and returns
     * true only if all SLO checks pass.
     *
     * For each SLO, the PrometheusResponse for its referenced SLI is truncated to
     * the first time series before being forwarded to the SLO checker, so that the
     * checker never receives multi-labeled data in a single request.
     */
    fun evaluate(
        load: Int,
        resource: Int,
        collectedSliData: Map<String, List<PrometheusResponse>>
    ): Boolean {
        val results = slos.map { slo ->
            val sliData = collectedSliData[slo.sli]
                ?: throw EvaluationFailedException(
                    "No collected data for SLI '${slo.sli}' referenced by SLO '${slo.name}'"
                )

            logger.info { "Evaluating SLO '${slo.name}' (SLI: '${slo.sli}') for load=$load, resource=$resource" }

            try {
                val sloChecker = SloCheckerFactory().create(slo = slo, load = load, resources = resource)
                // Forward only the first series per repetition to the checker.
                val firstSeriesOnly = sliData.map { response ->
                    PrometheusResponse(
                        status = response.status,
                        data = response.data?.let { data ->
                            PromData(
                                resultType = data.resultType,
                                result = data.result?.take(1)
                            )
                        }
                    )
                }
                sloChecker.evaluate(firstSeriesOnly)
            } catch (e: Exception) {
                throw EvaluationFailedException(
                    "Evaluation of SLO '${slo.name}' failed for resource=$resource, load=$load",
                    e
                )
            }
        }
        return false !in results
    }
}
