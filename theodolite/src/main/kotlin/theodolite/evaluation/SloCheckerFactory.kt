package theodolite.evaluation

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
        externalSlopeURL: String,
        threshold: Int,
        warmup: Int
    ): SloChecker {
        return when (sloType) {
            "lag trend" -> ExternalSloChecker(
                externalSlopeURL = externalSlopeURL,
                threshold = threshold,
                warmup = warmup
            )
            else -> throw IllegalArgumentException("Slotype $sloType not found.")
        }
    }
}
