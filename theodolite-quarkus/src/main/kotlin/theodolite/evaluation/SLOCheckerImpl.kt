package theodolite.evaluation

import khttp.post
import org.json.JSONObject
import java.net.ConnectException

class SLOCheckerImpl(private val prometheusURL: String) : SLOChecker {

    override fun evaluate(start: Long, end: Long): Boolean {
        var counter = 0
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL)
        val fetchedData = metricFetcher.fetchMetric(start, end, "sum by(group)(kafka_consumergroup_group_lag > 0)")
        val data = JSONObject(mapOf("total_lag" to fetchedData.data?.result))
        
        while (counter < 2) {
            val result = post("http://127.0.0.1:8000/evaluate-slope", data = data)
            if (result.statusCode != 200) {
                counter++
            } else {
                return result.text.toBoolean()
            }
        }

        throw ConnectException("Could not reach slope evaluation")
    }
}
