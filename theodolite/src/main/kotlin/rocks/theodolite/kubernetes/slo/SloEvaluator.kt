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
     * First-series trimming is handled by [MetricQueryResponse.getDataForSLOChecker]
     * inside [ExternalSloChecker], so full responses are forwarded here.
     */
    fun evaluate(
        load: Int,
        resource: Int,
        collectedSliData: Map<String, List<MetricQueryResponse>>
    ): Boolean {
        val results = slos.map { slo ->
            val sliData = collectedSliData[slo.sli]
                ?: throw EvaluationFailedException(
                    "No collected data for SLI '${slo.sli}' referenced by SLO '${slo.name}'"
                )

            logger.info { "Evaluating SLO '${slo.name}' (SLI: '${slo.sli}') for load=$load, resource=$resource" }

            try {
                val sloChecker = SloCheckerFactory().create(slo = slo, load = load, resources = resource)
                sloChecker.evaluate(sliData)
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
