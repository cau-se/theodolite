package theodolite.evaluation

import java.time.Duration

class SloCheckerFactory {

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
