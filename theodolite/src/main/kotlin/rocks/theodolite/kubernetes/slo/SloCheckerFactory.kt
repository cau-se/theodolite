package rocks.theodolite.kubernetes.slo

import net.objecthunter.exp4j.ExpressionBuilder
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

/**
 * Factory that creates an [ExternalSloChecker] from a resolved [Slo].
 */
class SloCheckerFactory {

    fun create(slo: Slo, load: Int, resources: Int): SloChecker {
        val url = slo.externalSloChecker ?: Configuration.SLO_CHECKER_URL

        val threshold = slo.threshold
            ?: slo.thresholdRelToLoad?.times(load)
            ?: slo.thresholdRelToResources?.times(resources)
            ?: slo.thresholdFromExpression?.let { eval(it, load, resources) }
            ?: throw IllegalArgumentException(
                "SLO '${slo.name}' requires one of: threshold, thresholdRelToLoad, " +
                    "thresholdRelToResources, thresholdFromExpression"
            )

        val metadata: MutableMap<String, Any> = mutableMapOf(
            "warmupSeconds" to slo.warmupSeconds,
            "threshold" to threshold
        )
        slo.queryAggregation?.let { metadata["queryAggregation"] = it }
        slo.repetitionAggregation?.let { metadata["repetitionAggregation"] = it }
        slo.operator?.let { metadata["operator"] = it }

        return ExternalSloChecker(externalSlopeURL = url, metadata = metadata)
    }

    private fun eval(expression: String, load: Int, resources: Int): Double {
        return ExpressionBuilder(expression)
            .variables("L", "R")
            .build()
            .setVariable("L", load.toDouble())
            .setVariable("R", resources.toDouble())
            .evaluate()
    }
}
