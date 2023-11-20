package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import java.net.ConnectException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
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
    private val TIMEOUT = Duration.ofSeconds(60)

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
    fun fetchMetric(start: Instant, end: Instant, stepSize: Duration, query: String): PrometheusResponse {

        val offsetStart = start.minus(offset)
        val offsetEnd = end.minus(offset)

        var counter = 0

        while (counter < RETRIES) {
            logger.info { "Request collected metrics from Prometheus for interval [$offsetStart,$offsetEnd]." }
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
            val request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "$prometheusURL/api/v1/query_range?query=$encodedQuery&start=$offsetStart&end=$offsetEnd&step={${stepSize.toSeconds()}}s"))
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(TIMEOUT)
                    .build()
            val response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                val message = response.body()
                logger.warn { "Could not connect to Prometheus: $message. Retry $counter/$RETRIES." }
                counter++
            } else {
                val values = parseValues(response.body())
                if (values.data?.result.isNullOrEmpty()) {
                    throw NoSuchFieldException("Empty query result: $values between for query '$query' in interval [$offsetStart,$offsetEnd] .")
                }
                return parseValues(response.body())
            }
        }
        throw ConnectException("No answer from Prometheus received.")
    }

    /**
     * Deserializes a response from Prometheus.
     * @param values Response from Prometheus.
     * @return a [PrometheusResponse]
     */
    private fun parseValues(values: String): PrometheusResponse {
        return ObjectMapper().readValue<PrometheusResponse>(
            values,
            PrometheusResponse::class.java
        )
    }
}
