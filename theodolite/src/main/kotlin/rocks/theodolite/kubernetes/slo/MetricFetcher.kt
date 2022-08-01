package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import khttp.get
import khttp.responses.Response
import mu.KotlinLogging
import rocks.theodolite.kubernetes.util.PrometheusResponse
import java.net.ConnectException
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Used to fetch metrics from Prometheus.
 * @param prometheusURL URL to the Prometheus server.
 * @param offset Duration of time that the start and end points of the queries
 * should be shifted. (for different timezones, etc..)
 */
class MetricFetcher(private val prometheusURL: String, private val offset: Duration) {
    private val RETRIES = 2
    private val TIMEOUT = 60.0

    /**
     * Tries to fetch a metric by a query to a Prometheus server.
     * Retries to fetch the metric [RETRIES] times.
     * Connects to the server via [prometheusURL].
     *
     * @param start start point of the query.
     * @param end end point of the query.
     * @param query query for the prometheus server.
     * @throws ConnectException - if the prometheus server timed out/was not reached.
     */
    fun fetchMetric(start: Instant, end: Instant, query: String): PrometheusResponse {

        val offsetStart = start.minus(offset)
        val offsetEnd = end.minus(offset)

        var counter = 0
        val parameter = mapOf(
            "query" to query,
            "start" to offsetStart.toString(),
            "end" to offsetEnd.toString(),
            "step" to "5s"
        )

        while (counter < RETRIES) {
            logger.info { "Request collected metrics from Prometheus for interval [$offsetStart,$offsetEnd]." }
            val response = get("$prometheusURL/api/v1/query_range", params = parameter, timeout = TIMEOUT)
            if (response.statusCode != 200) {
                val message = response.jsonObject.toString()
                logger.warn { "Could not connect to Prometheus: $message. Retry $counter/$RETRIES." }
                counter++
            } else {
                val values = parseValues(response)
                if (values.data?.result.isNullOrEmpty()) {
                    throw NoSuchFieldException("Empty query result: $values between for query '$query' in interval [$offsetStart,$offsetEnd] .")
                }
                return parseValues(response)
            }
        }
        throw ConnectException("No answer from Prometheus received.")
    }

    /**
     * Deserializes a response from Prometheus.
     * @param values Response from Prometheus.
     * @return a [PrometheusResponse]
     */
    private fun parseValues(values: Response): PrometheusResponse {
        return ObjectMapper().readValue<PrometheusResponse>(
            values.jsonObject.toString(),
            PrometheusResponse::class.java
        )
    }
}
