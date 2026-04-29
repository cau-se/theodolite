package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

/**
 * [SloChecker] that delegates evaluation to an external HTTP service.
 *
 * @param externalSlopeURL URL of the external SLO checker service (per-SLO).
 * @param metadata metadata passed to the external checker (warmupSeconds, threshold, etc.).
 */
class ExternalSloChecker(
    val externalSlopeURL: String,
    val metadata: Map<String, Any>
) : SloChecker {

    private val retries = 2
    private val timeout = Duration.ofSeconds(60)
    private val logger = KotlinLogging.logger {}

    /**
     * Sends [fetchedData] to the external service and returns its pass/fail verdict.
     * Only the first time series of each response is included in the payload.
     *
     * @throws ConnectException if the external service cannot be reached after [retries] attempts.
     */
    override fun evaluate(fetchedData: List<MetricQueryResponse>): Boolean {
        var counter = 0
        val data = SloJson(
            results = fetchedData.map { it.getDataForSLOChecker(onlyFirst = true) },
            metadata = metadata
        ).toJson()

        while (counter < retries) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(externalSlopeURL))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(timeout)
                .build()
            val response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString())
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                counter++
                logger.error { "Received status code ${response.statusCode()} for request to $externalSlopeURL." }
            } else {
                val booleanResult = response.body().toBoolean()
                logger.info { "SLO checker result is: $booleanResult." }
                return booleanResult
            }
        }

        throw ConnectException("Could not reach external SLO checker at $externalSlopeURL.")
    }
}
