package rocks.theodolite.kubernetes.slo

import net.objecthunter.exp4j.ExpressionBuilder
import rocks.theodolite.core.strategies.Metric
import kotlin.math.pow


/**
 * Factory used to potentially create different [SloChecker]s.
 * Supports: lag type.
 */
class SloCheckerFactory {

    /**
     * Creates different [SloChecker]s.
     *
     * Supports: `lag trend` and `lag trend percent` as arguments for `sloType`
     *
     * ### `lag trend`
     * Creates an [ExternalSloChecker] with defined parameters.
     *
     * The properties map needs the following fields:
     * - `externalSlopeURL`: Url to the concrete SLO checker service.
     * - `threshold`: fixed value used for the slope.
     * - `warmup`: time from the beginning to skip in the analysis.
     *
     *
     * ### `lag trend ratio`
     * Creates an [ExternalSloChecker] with defined parameters.
     * The required threshold is computed using a ratio and the load of the experiment.
     *
     * The properties map needs the following fields:
     * - `externalSlopeURL`: Url to the concrete SLO checker service.
     * - `ratio`: of the executed load that is accepted for the slope.
     * - `warmup`: time from the beginning to skip in the analysis.
     *
     * @param sloType Type of the [SloChecker].
     * @param properties map of properties to use for the SLO checker creation.
     * @param load Load that is generated in the experiment.
     * @param resources Resources that are used in the experiment.
     * @param metric Metric used in the benchmark execution.
     *
     * @return A [SloChecker]
     * @throws IllegalArgumentException If [sloType] not supported.
     */
    fun create(
        sloType: String,
        properties: Map<String, String>,
        load: Int,
        resources: Int,
        metric: Metric
    ): SloChecker =
        when (SloTypes.from(sloType)) {
            SloTypes.GENERIC -> ExternalSloChecker(
                externalSlopeURL = properties["externalSloUrl"]
                    ?: throw IllegalArgumentException("externalSloUrl expected"),
                // TODO validate property contents
                metadata = mapOf(
                    "warmup" to (properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")),
                    "queryAggregation" to (properties["queryAggregation"]
                        ?: throw IllegalArgumentException("queryAggregation expected")),
                    "repetitionAggregation" to (properties["repetitionAggregation"]
                        ?: throw IllegalArgumentException("repetitionAggregation expected")),
                    "operator" to (properties["operator"] ?: throw IllegalArgumentException("operator expected")),
                    "threshold" to (properties["threshold"]?.toDoubleOrNull()
                        ?: properties["thresholdRelToLoad"]?.toDoubleOrNull()?.times(load)
                        ?: properties["thresholdRelToResources"]?.toDoubleOrNull()?.times(resources)
                        ?: properties["thresholdFromExpression"]?.let { this.eval(it, load, resources) }
                        ?: throw IllegalArgumentException("'threshold', 'thresholdRelToLoad' or 'thresholdRelToResources' or 'thresholdFromExpression' expected"))
                )
            )
            SloTypes.LAG_TREND, SloTypes.DROPPED_RECORDS -> ExternalSloChecker(
                externalSlopeURL = properties["externalSloUrl"]
                    ?: throw IllegalArgumentException("externalSloUrl expected"),
                metadata = mapOf(
                    "warmup" to (properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")),
                    "threshold" to (properties["threshold"]?.toDoubleOrNull()
                        ?: properties["thresholdRelToLoad"]?.toDoubleOrNull()?.times(load)
                        ?: properties["thresholdRelToResources"]?.toDoubleOrNull()?.times(resources)
                        ?: properties["thresholdFromExpression"]?.let { this.eval(it, load, resources) }
                        ?: throw IllegalArgumentException("Valid 'threshold', 'thresholdRelToLoad' or 'thresholdRelToResources' or 'thresholdFromExpression' expected"))
                )
            )
            SloTypes.LAG_TREND_RATIO, SloTypes.DROPPED_RECORDS_RATIO -> {
                val thresholdRatio =
                    properties["ratio"]?.toDouble()
                        ?: throw IllegalArgumentException("ratio for threshold expected")
                if (thresholdRatio < 0.0) {
                    throw IllegalArgumentException("Threshold ratio needs to be an Double greater or equal 0.0")
                }

                val threshold = (load * thresholdRatio)

                ExternalSloChecker(
                    externalSlopeURL = properties["externalSloUrl"]
                        ?: throw IllegalArgumentException("externalSloUrl expected"),
                    metadata = mapOf(
                        "warmup" to (properties["warmup"]?.toInt()
                            ?: throw IllegalArgumentException("warmup expected")),
                        "threshold" to threshold
                    )
                )
            }

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
