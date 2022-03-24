package theodolite.evaluation

import khttp.post
import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.net.ConnectException

/**
 * [SloChecker] that uses an external source for the concrete evaluation.
 * @param externalSlopeURL The url under which the external evaluation can be reached.
 * @param metadata metadata passed to the external SLO checker.
 */
class ExternalSloChecker(
    private val externalSlopeURL: String,
    private val metadata: Map<String, Any>
) : SloChecker {

    private val RETRIES = 2
    private val TIMEOUT = 60.0

    private val logger = KotlinLogging.logger {}

    /**
     * Evaluates an experiment using an external service.
     * Will try to reach the external service until success or [RETRIES] times.
     * Each request will timeout after [TIMEOUT].
     *
     * @param fetchedData that should be evaluated
     * @return true if the experiment was successful (the threshold was not exceeded).
     * @throws ConnectException if the external service could not be reached.
     */
    override fun evaluate(fetchedData: List<PrometheusResponse>): Boolean {
        var counter = 0
        val data = SloJson(
            results = fetchedData.map { it.data?.result ?: listOf() },
            metadata = metadata
        ).toJson()

        while (counter < RETRIES) {
            val result = post(externalSlopeURL, data = data, timeout = TIMEOUT)
            if (result.statusCode != 200) {
                counter++
                logger.error { "Could not reach external SLO checker at $externalSlopeURL." }
            } else {
                val booleanResult = result.text.toBoolean()
                logger.info { "SLO checker result is: $booleanResult." }
                return booleanResult
            }
        }

        throw ConnectException("Could not reach external SLO checker at $externalSlopeURL.")
    }
}
