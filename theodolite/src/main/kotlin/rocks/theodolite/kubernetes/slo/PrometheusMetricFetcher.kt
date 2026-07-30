package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli
import java.net.ConnectException
import java.net.HttpURLConnection
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
 * Fetches metrics from a Prometheus server.
 *
 * Provider config keys read from [Sli.providerConfig]:
 * - `prometheusUrl` — URL to the Prometheus instance (falls back to [Configuration.PROMETHEUS_URL])
 * - `offsetHours`   — timezone offset applied to query windows (falls back to [Configuration.PROMETHEUS_OFFSET_HOURS])
 */
class PrometheusMetricFetcher(sli: Sli) : MetricFetcher {
    private val prometheusUrl: String =
        sli.providerConfig["prometheusUrl"] ?: Configuration.PROMETHEUS_URL
    private val offset: Duration =
        Duration.ofHours(sli.providerConfig["offsetHours"]?.toLongOrNull() ?: Configuration.PROMETHEUS_OFFSET_HOURS)

    private val retries = 2
    private val timeout = Duration.ofSeconds(60)

    override fun fetchMetric(start: Instant, end: Instant, stepSize: Duration, query: String): MetricQueryResponse {
        val offsetStart = start.minus(offset)
        val offsetEnd = end.minus(offset)
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)

        var counter = 0
        while (counter < retries) {
            logger.info { "Request collected metrics from Prometheus for interval [$offsetStart,$offsetEnd]." }
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$prometheusUrl/api/v1/query_range?query=$encodedQuery&start=$offsetStart&end=$offsetEnd&step=${stepSize.toSeconds()}s"))
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(timeout)
                .build()
            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                logger.warn { "Could not connect to Prometheus: ${response.body()}. Retry $counter/$retries." }
                counter++
            } else {
                val parsed = PrometheusResponse.fromString(response.body())
                if (parsed.isNullOrEmpty()) {
                    throw NoSuchFieldException(
                        "Empty query result for query '$query' in interval [$offsetStart,$offsetEnd]."
                    )
                }
                return parsed
            }
        }
        throw ConnectException("No answer from Prometheus received.")
    }
}
