package theodolite.evaluation

import com.google.gson.Gson
import khttp.get
import khttp.responses.Response
import theodolite.util.PrometheusResponse
import java.net.ConnectException
import java.util.*


class MetricFetcher(private val prometheusURL: String) {

    fun fetchMetric(start: Long, end: Long, query: String): PrometheusResponse {
        var trys = 0
        val parameter = mapOf(
            "query" to query,
            "start" to toISODate(start),
            "end" to toISODate(end),
            "step" to "5s"
        )

        while (trys < 2) {
            val response = get("$prometheusURL/api/v1/query_range", params = parameter)
            if (response.statusCode != 200) {
                trys++
            } else {
                return parseValues(response)
            }
        }
        throw ConnectException("No answer from Prometheus received")
    }

    private fun toISODate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
        val date = Date(timestamp - (3600 * 1000))//subtract 1h since cluster is in another timezone
        return sdf.format(date)
    }

    private fun parseValues(values: Response): PrometheusResponse {
        return Gson().fromJson<PrometheusResponse>(
            values.jsonObject.toString(),
            PrometheusResponse::class.java
        )
    }
}
