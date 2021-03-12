package theodolite.evaluation

import com.google.gson.Gson
import khttp.post
import java.net.ConnectException
import java.time.Duration
import java.time.Instant

class ExternalSLOChecker(
    private val prometheusURL: String,
    private val query: String,
    private val externalSlopeURL: String,
    private val threshold: Int,
    private val offset: Duration,
    private val warmup: Int
) :
    SloChecker {

    override fun evaluate(start: Instant, end: Instant): Boolean {
        var counter = 0
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL, offset = offset)
        val fetchedData = metricFetcher.fetchMetric(start, end, query)
        val data =
            Gson().toJson(mapOf("total_lag" to fetchedData.data?.result, "threshold" to threshold, "warmup" to warmup))

        while (counter < 2) {
            val result = post(externalSlopeURL, data = data, timeout = 60.0)
            if (result.statusCode != 200) {
                counter++
            } else {
                return result.text.toBoolean()
            }
        }

        throw ConnectException("Could not reach slope evaluation")
    }
}
