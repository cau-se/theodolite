package theodolite.evaluation

import com.google.gson.Gson
import khttp.post
import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.net.ConnectException
import java.time.Duration
import java.time.Instant

class ExternalSloChecker(
    private val prometheusURL: String,
    private val query: String,
    private val externalSlopeURL: String,
    private val threshold: Int,
    private val offset: Duration,
    private val warmup: Int
) :
    SloChecker {

    private val RETRIES = 2
    private val TIMEOUT = 60.0

    private val logger = KotlinLogging.logger {}

    override fun evaluate(start: Instant, end: Instant, fetchedData: PrometheusResponse): Boolean {
        var counter = 0
        val data =
            Gson().toJson(mapOf("total_lag" to fetchedData.data?.result, "threshold" to threshold, "warmup" to warmup))

        while (counter < RETRIES) {
            val result = post(externalSlopeURL, data = data, timeout = TIMEOUT)
            if (result.statusCode != 200) {
                counter++
                logger.error{"Could not reach external slope analysis"}
            } else {
                return result.text.toBoolean()
            }
        }

        throw ConnectException("Could not reach slope evaluation")
    }
}
