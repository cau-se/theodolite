package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
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

class PrometheusRequestExecutor : MetricRequestExecutor {
    override fun executeRequest(
        uri: URI,
        query: String,
        offsetStart: Instant,
        offsetEnd: Instant,
        stepSize: Duration,
        timeout: Duration
    ): MetricQueryResponse {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)

        val request = HttpRequest.newBuilder()
            .uri(URI.create(
                "$uri/api/v1/query_range?query=$encodedQuery&start=$offsetStart&end=$offsetEnd&step=${stepSize.toSeconds()}s"))
            .GET()
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()

        logger.info { "Request collected metrics from Prometheus for interval [$offsetStart,$offsetEnd]." }

        val response = HttpClient
            .newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !=  HttpURLConnection.HTTP_OK) {
            val message = response.body()
            throw ConnectException("Could not connect to Metric Provider Prometheus: $message.")
        } else {
            return PrometheusResponse.fromString(response.body())
        }

    }
}
