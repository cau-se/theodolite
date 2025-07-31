package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.eclipse.microprofile.config.ConfigProvider
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

class DQLRequestExecutor : MetricRequestExecutor {
    private var clientId: String = ConfigProvider.getConfig().getValue("dql.clientid", String::class.java)
    private var clientSecret: String = ConfigProvider.getConfig().getValue("dql.clientsecret", String::class.java)
    private var scope: String = ConfigProvider.getConfig().getValue("dql.scope", String::class.java)
    private var resource: String = ConfigProvider.getConfig().getValue("dql.resource", String::class.java)
    private var authURI: URI = ConfigProvider.getConfig().getValue("dql.authurl", URI::class.java)

    override fun executeRequest(
        uri: URI,
        query: String,
        offsetStart: Instant,
        offsetEnd: Instant,
        stepSize: Duration,
        timeout: Duration
    ): MetricQueryResponse {
        val authToken = requestOAuth(timeout)

        val requestToken = executeQuery(uri, query, offsetStart, offsetEnd, timeout, authToken)

        var tries = 0
        val maxTries = 5
        var pollResponse : DQLPollResponse
        do {
            Thread.sleep(500)
            val queryResultJson = pollQueryResults(uri, authToken, timeout, requestToken)
            pollResponse = DQLPollResponse.fromString(queryResultJson)
            tries++
        } while (!pollResponse.isSuccessful() && tries < maxTries)

        return pollResponse
    }

    private fun requestOAuth(timeout: Duration): String{
        val params = mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "grant_type" to "client_credentials",
            "scope" to scope,
            "resource" to resource
        )
        val encodedParams = params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, StandardCharsets.UTF_8)}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
        }

        val request = HttpRequest.newBuilder()
            .uri(authURI)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encodedParams))
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()

        val response = HttpClient
            .newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw ConnectException("Could not connect to Dynatrace OAuth: ${response.body()}")
        }else{
            data class AuthResponse(
                val scope: String,
                val token_type: String,
                val expires_in: Int,
                val access_token: String,
                val resource: String
            )
            val authResponse : AuthResponse =  jacksonObjectMapper().readValue(response.body())

            return authResponse.access_token
        }
    }

    private fun executeQuery(uri: URI, query: String, offsetStart: Instant, offsetEnd: Instant, timeout: Duration, authToken: String): String {
        logger.info { "Request collected metrics from Dynatrace for interval [$offsetStart,$offsetEnd]." }

        val execRequest = HttpRequest.newBuilder()
            .uri(URI.create("$uri:execute"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $authToken")
            .POST(HttpRequest.BodyPublishers.ofString("{\"query\": \"$query, timeframe: toTimeframe(\\\"$offsetStart/$offsetEnd\\\")\"}"))
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()

        val response = HttpClient
            .newHttpClient()
            .send(execRequest, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            val message = response.body()
            throw ConnectException("Could not execute DQL Query: $message.")
        } else {
            data class ExecuteResponse(
                val state: String,
                val requestToken: String,
                val ttlSeconds: Int
            )
            val execResponse : ExecuteResponse =  jacksonObjectMapper().readValue(response.body())

            return execResponse.requestToken
        }
    }

    private fun pollQueryResults(uri: URI, authToken: String, timeout: Duration, requestToken : String) : String{
        val encodedToken = URLEncoder.encode(requestToken, StandardCharsets.UTF_8)
        val execRequest = HttpRequest.newBuilder()
            .uri(URI.create("$uri:poll?request-token=$encodedToken"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $authToken")
            .GET()
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .build()

        return HttpClient
            .newHttpClient()
            .send(execRequest, HttpResponse.BodyHandlers.ofString()).body()
    }
}

