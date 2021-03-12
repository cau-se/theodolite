package theodolite.evaluation

import com.google.gson.Gson
import khttp.get
import khttp.responses.Response
import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.net.ConnectException
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

class MetricFetcher(private val prometheusURL: String, private val offset: Duration) {

    fun fetchMetric(start: Instant, end: Instant, query: String): PrometheusResponse {

        val offsetStart = start.minus(offset)
        val offsetEnd = end.minus(offset)

        var trys = 0
        val parameter = mapOf(
            "query" to query,
            "start" to offsetStart.toString(),
            "end" to offsetEnd.toString(),
            "step" to "5s"
        )

        while (trys < 2) {
            val response = get("$prometheusURL/api/v1/query_range", params = parameter, timeout = 60.0)
            if (response.statusCode != 200) {
                val message = response.jsonObject.toString()
                logger.warn { "Could not connect to Prometheus: $message, retrying now" }
                trys++
            } else {
                val values = parseValues(response)
                if (values.data?.result.isNullOrEmpty()) {
                    logger.error { "Empty query result: $values" }
                    throw NoSuchFieldException()
                }
                return parseValues(response)
            }
        }
        throw ConnectException("No answer from Prometheus received")
    }

    private fun parseValues(values: Response): PrometheusResponse {
        return Gson().fromJson<PrometheusResponse>(
            values.jsonObject.toString(),
            PrometheusResponse::class.java
        )
    }
}
