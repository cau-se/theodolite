package theodolite.evaluation

/**
 * Factory used to potentially create different SloCheckers.
 * Supports: lag type.
 */
class SloCheckerFactory {

    /**
     * Creates different [SloChecker]s.
     * Supports: lag type.
     *
     * @param slotype Type of the SloChecker.
     * @param externalSlopeURL Url to the concrete SlopeChecker.
     * @param threshold for the SloChecker.
     * @param warmup for the sloChecker.
     *
     * @return A SloChecker
     * @throws IllegalArgumentException If sloType not supported.
     */
    fun create(
        slotype: String,
        externalSlopeURL: String,
        threshold: Int,
        warmup: Int
    ): SloChecker {
        return when (slotype) {
            "lag trend" -> ExternalSloChecker(
                externalSlopeURL = externalSlopeURL,
                threshold = threshold,
                warmup = warmup
            )
            else -> throw IllegalArgumentException("Slotype $slotype not found.")
        }
    }
}
