package theodolite.evaluation

import com.google.gson.Gson
import khttp.post
import java.net.ConnectException
import java.time.Duration
import java.time.Instant

class SLOCheckerImpl(private val prometheusURL: String, private val threshold: Int, private val offset: Duration) :
    SLOChecker {

    override fun evaluate(start: Instant, end: Instant): Boolean {
        var counter = 0
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL, offset = offset)
        val fetchedData = metricFetcher.fetchMetric(start, end, "sum by(group)(kafka_consumergroup_group_lag >= 0)")
        val data = Gson().toJson(mapOf("total_lag" to fetchedData.data?.result, "threshold" to threshold))

        while (counter < 2) {
            val result = post("http://127.0.0.1:8000/evaluate-slope", data = data, timeout = 60.0)
            if (result.statusCode != 200) {
                counter++
            } else {
                return result.text.toBoolean()
            }
        }

        throw ConnectException("Could not reach slope evaluation")
    }
}
