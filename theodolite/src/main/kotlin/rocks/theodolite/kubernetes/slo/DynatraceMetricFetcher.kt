package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.eclipse.microprofile.config.ConfigProvider
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
 * Fetches metrics from Dynatrace using DQL (Dynatrace Query Language).
 *
 * Provider config keys read from [Sli.providerConfig]:
 * - `dynatraceUrl` — base URL of the Dynatrace DQL query API endpoint
 *   (e.g. `https://<tenant>.apps.dynatrace.com/platform/storage/query/v1/query`).
 *   Falls back to the `THEODOLITE_DYNATRACE_URL` environment variable / [Configuration.DYNATRACE_URL].
 *
 * OAuth credentials are read from MicroProfile config (environment variables or application.properties):
 * - `dql.clientid`, `dql.clientsecret`, `dql.scope`, `dql.resource`, `dql.authurl`
 *
 * Credentials are resolved lazily on the first call to [fetchMetric] so that Prometheus-only
 * deployments are not required to provide Dynatrace configuration.
 */
class DynatraceMetricFetcher(sli: Sli) : MetricFetcher {
    private val queryUri: URI = URI.create(
        sli.providerConfig["dynatraceUrl"]
            ?: Configuration.DYNATRACE_URL
            ?: error(
                "Dynatrace SLI '${sli.name}' requires either providerConfig.dynatraceUrl " +
                    "or the THEODOLITE_DYNATRACE_URL environment variable."
            )
    )

    private val timeout = Duration.ofSeconds(60)
    private val pollIntervalMs = 500L
    private val maxPolls = 5

    // OAuth credentials resolved lazily to avoid startup failure when DQL is unused.
    private val clientId: String by lazy { ConfigProvider.getConfig().getValue("dql.clientid", String::class.java) }
    private val clientSecret: String by lazy { ConfigProvider.getConfig().getValue("dql.clientsecret", String::class.java) }
    private val scope: String by lazy { ConfigProvider.getConfig().getValue("dql.scope", String::class.java) }
    private val resource: String by lazy { ConfigProvider.getConfig().getValue("dql.resource", String::class.java) }
    private val authUri: URI by lazy { ConfigProvider.getConfig().getValue("dql.authurl", URI::class.java) }

    override fun fetchMetric(start: Instant, end: Instant, stepSize: Duration, query: String): MetricQueryResponse {
        val authToken = requestOAuth()
        val requestToken = executeQuery(query, start, end, authToken)

        var tries = 0
        var pollResponse: DqlPollResponse
        do {
            Thread.sleep(pollIntervalMs)
            val json = pollQueryResults(authToken, requestToken)
            pollResponse = DqlPollResponse.fromString(json)
            tries++
        } while (!pollResponse.isSuccessful() && tries < maxPolls)

        if (pollResponse.isNullOrEmpty()) {
            throw NoSuchFieldException(
                "Empty DQL result for query '$query' in interval [$start,$end]."
            )
        }
        return pollResponse
    }

    private fun requestOAuth(): String {
        val params = mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "grant_type" to "client_credentials",
            "scope" to scope,
            "resource" to resource
        )
        val encodedParams = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, StandardCharsets.UTF_8)}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
        }
        val request = HttpRequest.newBuilder()
            .uri(authUri)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encodedParams))
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw ConnectException("Could not connect to Dynatrace OAuth: ${response.body()}")
        }
        data class AuthResponse(val scope: String, val token_type: String, val expires_in: Int, val access_token: String, val resource: String)
        return jacksonObjectMapper().readValue<AuthResponse>(response.body()).access_token
    }

    private fun executeQuery(query: String, start: Instant, end: Instant, authToken: String): String {
        logger.info { "Requesting metrics from Dynatrace for interval [$start,$end]." }
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$queryUri:execute"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $authToken")
            .POST(HttpRequest.BodyPublishers.ofString(
                """{"query": "$query", "defaultTimeframeStart": "$start", "defaultTimeframeEnd": "$end"}"""
            ))
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw ConnectException("Could not execute DQL query: ${response.body()}.")
        }
        data class ExecuteResponse(val state: String, val requestToken: String, val ttlSeconds: Int)
        return jacksonObjectMapper().readValue<ExecuteResponse>(response.body()).requestToken
    }

    private fun pollQueryResults(authToken: String, requestToken: String): String {
        val encodedToken = URLEncoder.encode(requestToken, StandardCharsets.UTF_8)
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$queryUri:poll?request-token=$encodedToken"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $authToken")
            .GET()
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body()
    }
}
