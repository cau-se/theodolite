package theodolite.evaluation

import com.google.gson.Gson
import khttp.post
import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.net.ConnectException
import java.time.Instant

/**
 * [SloChecker] that uses an external source for the concrete evaluation.
 * @param externalSlopeURL The url under which the external evaluation can be reached.
 * @param threshold threshold that should not be exceeded to evaluate to true.
 * @param warmup time that is not taken into consideration for the evaluation.
 */
class ExternalSloChecker(
    private val externalSlopeURL: String,
    private val threshold: Int,
    private val warmup: Int
) : SloChecker {

    private val RETRIES = 2
    private val TIMEOUT = 60.0

    private val logger = KotlinLogging.logger {}

    /**
     * Evaluates an experiment using an external service.
     * Will try to reach the external service until success or [RETRIES] times.
     * Each request will timeout after [TIMEOUT].
     *
     * @param start point of the experiment.
     * @param end point of the experiment.
     * @param fetchedData that should be evaluated
     * @return true if the experiment was successful(the threshold was not exceeded.
     * @throws ConnectException if the external service could not be reached.
     */
    override fun evaluate(fetchedData: List<PrometheusResponse>): Boolean {
        var counter = 0
        val data = Gson().toJson(mapOf(
            "total_lags" to fetchedData.map { it.data?.result},
            "threshold" to threshold,
            "warmup" to warmup))

        while (counter < RETRIES) {
            val result = post(externalSlopeURL, data = data, timeout = TIMEOUT)
            if (result.statusCode != 200) {
                counter++
                logger.error { "Could not reach external SLO checker" }
            } else {
                val booleanResult = result.text.toBoolean()
                logger.info { "SLO checker result is: $booleanResult" }
                return booleanResult
            }
        }

        throw ConnectException("Could not reach external SLO checker")
    }
}
