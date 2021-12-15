package theodolite.evaluation

import theodolite.util.LoadDimension

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
     * @param load that is executed in the experiment.
     *
     * @return A [SloChecker]
     * @throws IllegalArgumentException If [sloType] not supported.
     */
    fun create(
        sloType: String,
        properties: MutableMap<String, String>,
        load: LoadDimension
    ): SloChecker {
        return when (sloType.lowercase()) {
            SloTypes.LAG_TREND.value, SloTypes.DROPPED_RECORDS.value -> ExternalSloChecker(
                externalSlopeURL = properties["externalSloUrl"]
                    ?: throw IllegalArgumentException("externalSloUrl expected"),
                threshold = properties["threshold"]?.toInt() ?: throw IllegalArgumentException("threshold expected"),
                warmup = properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")
            )

                SloTypes.LAG_TREND_RATIO.value, SloTypes.DROPPED_RECORDS_RATIO.value -> {
                val thresholdRatio =
                    properties["ratio"]?.toDouble()
                        ?: throw IllegalArgumentException("ratio for threshold expected")
                if (thresholdRatio < 0.0) {
                    throw IllegalArgumentException("Threshold ratio needs to be an Double greater or equal 0.0")
                }
                // cast to int, as rounding is not really necessary
                val threshold = (load.get() * thresholdRatio).toInt()

                ExternalSloChecker(
                    externalSlopeURL = properties["externalSloUrl"]
                        ?: throw IllegalArgumentException("externalSloUrl expected"),
                    threshold = threshold,
                    warmup = properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")
                )
            }
            else -> throw IllegalArgumentException("Slotype $sloType not found.")
        }
    }
}
