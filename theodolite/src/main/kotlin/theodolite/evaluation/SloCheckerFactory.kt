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
     * ### `lag trend percent`
     * Creates an [ExternalSloChecker] with defined parameters.
     * The required threshold is computed using a percentage and the load of the experiment.
     *
     * The properties map needs the following fields:
     * - `externalSlopeURL`: Url to the concrete SLO checker service.
     * - `percent`: of the executed load that is accepted for the slope.
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
        return when (sloType.toLowerCase()) {
            SloTypes.LAG_TREND.value, SloTypes.DROPPED_RECORDS.value -> ExternalSloChecker(
                externalSlopeURL = properties["externalSloUrl"]
                    ?: throw IllegalArgumentException("externalSloUrl expected"),
                threshold = properties["threshold"]?.toInt() ?: throw IllegalArgumentException("threshold expected"),
                warmup = properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")
            )
            SloTypes.LAG_TREND_PERCENTAGE.value, SloTypes.DROPPED_RECORDS_PERCENTAGE.value -> {
                if (!properties["loadType"].equals("NumSensors")) {
                    throw IllegalArgumentException("Percent Threshold is only allowed with load type NumSensors")
                }
                val thresholdPercent =
                    properties["percent"]?.toDouble()
                        ?: throw IllegalArgumentException("percent for threshold expected")
                if (thresholdPercent < 0.0 || thresholdPercent > 1.0) {
                    throw IllegalArgumentException("Threshold percent need to be an Double in the range between 0.0 and 1.0 (inclusive)")
                }
                // cast to int, as rounding is not really necessary
                val threshold = (load.get() * thresholdPercent).toInt()

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
