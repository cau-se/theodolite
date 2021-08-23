package theodolite.evaluation

import theodolite.util.LoadDimension

/**
 * Factory used to potentially create different [SloChecker]s.
 * Supports: lag type.
 */
class SloCheckerFactory {

    /**
     * Creates different [SloChecker]s.
     * Supports: lag type.
     *
     * @param sloType Type of the [SloChecker].
     * @param externalSlopeURL Url to the concrete [SloChecker].
     * @param threshold for the [SloChecker].
     * @param warmup for the [SloChecker].
     *
     * @return A [SloChecker]
     * @throws IllegalArgumentException If [sloType] not supported.
     */
    fun create(
        sloType: String,
        properties: MutableMap<String, String>,
        load: LoadDimension
    ): SloChecker {
        return when (sloType) {
            "lag trend" -> ExternalSloChecker(
                externalSlopeURL = properties["externalSloUrl"]
                    ?: throw IllegalArgumentException("externalSloUrl expected"),
                threshold = properties["threshold"]?.toInt() ?: throw IllegalArgumentException("threshold expected"),
                warmup = properties["warmup"]?.toInt() ?: throw IllegalArgumentException("warmup expected")
            )
            "lag trend percent" -> {
                if (!properties["loadType"].equals("NumSensors")) {
                    throw IllegalArgumentException("Percent Threshold is only allowed with load type NumSensors")
                }
                var thresholdPercent =
                    properties["percent"]?.toInt() ?: throw IllegalArgumentException("percent for threshold expected")
                if (thresholdPercent < 0 || thresholdPercent > 100) {
                    throw IllegalArgumentException("Threshold percent need to be an Int in the range between 0 and 100 (inclusive)")
                }
                var threshold = (load.get() / 100.0 * thresholdPercent).toInt()

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
