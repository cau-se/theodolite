package theodolite.evaluation

import java.time.Duration

class SloCheckerFactory {

    fun create(
        slotype: String,
        prometheusURL: String,
        query: String,
        externalSlopeURL: String,
        threshold: Int,
        offset: Duration,
        warmup: Int
    ): SloChecker {

        return when (slotype) {
            "lag trend" -> ExternalSloChecker(
                prometheusURL = prometheusURL,
                query = query,
                externalSlopeURL = externalSlopeURL,
                threshold = threshold,
                offset = offset,
                warmup = warmup
            )
            else -> throw IllegalArgumentException("Slotype $slotype not found.")
        }
    }
}
