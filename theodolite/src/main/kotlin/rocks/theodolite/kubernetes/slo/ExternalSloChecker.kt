package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
import org.eclipse.microprofile.config.ConfigProvider
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration


/**
 * [SloChecker] that uses an external source for the concrete evaluation.
 * @param metadata metadata passed to the external SLO checker.
 */
class ExternalSloChecker(
    val metadata: Map<String, Any>
) : SloChecker {
    private val externalSLOURL: URI = ConfigProvider.getConfig().getValue("slo.checker.url", URI::class.java)
    private val RETRIES = 2
    private val TIMEOUT = Duration.ofSeconds(60)

    private val logger = KotlinLogging.logger {}

    /**
     * Evaluates an experiment using an external service.
     * Will try to reach the external service until success or [RETRIES] times.
     * Each request will time out after [TIMEOUT].
     *
     * @param fetchedData that should be evaluated
     * @return true if the experiment was successful (the threshold was not exceeded).
     * @throws ConnectException if the external service could not be reached.
     */
    override fun evaluate(fetchedData: List<MetricQueryResponse>): Boolean {
        var counter = 0
        val data = SloJson(
            results = fetchedData.map { it.getDataForSLOChecker() },
            metadata = metadata
        ).toJson()

        while (counter < RETRIES) {
            val request = HttpRequest.newBuilder()
                    .uri(externalSLOURL)
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(TIMEOUT)
                    .build()
            val response = HttpClient
                .newHttpClient()
                .send(request, BodyHandlers.ofString())
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                counter++
                logger.error { "Received status code ${response.statusCode()} for request to $externalSLOURL." }
            } else {
                val booleanResult = response.body().toBoolean()
                logger.info { "SLO checker result is: $booleanResult." }
                return booleanResult
            }
        }

        throw ConnectException("Could not reach external SLO checker at $externalSLOURL.")
    }
}
