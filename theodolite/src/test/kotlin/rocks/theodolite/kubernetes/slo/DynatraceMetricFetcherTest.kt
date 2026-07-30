package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@QuarkusTest
@QuarkusTestResource(WireMockTestResource::class)
internal class DynatraceMetricFetcherTest {

    private val exampleDateTime = LocalDateTime.of(2023, 7, 24, 10, 22, 0).toInstant(ZoneOffset.UTC)

    private val queryPath = "/platform/storage/query/v1/query"

    /**
     * A real-world, multi-line DQL query containing newlines, double quotes and single quotes.
     * Building the execute-request body via naive string interpolation produces invalid JSON for
     * such a query, which is exactly the bug these tests guard against.
     */
    private val multilineDqlQuery = """
        fetch logs
        | filter contains(k8s.workload.name, "blobcachebench-")
        | filter log.logger == "BenchmarkResultWriter"
        | parse message, "'summary total_runtime_ms='INT:runtime_ms' canceled='INT:canceled"
        | makeTimeseries avg(canceled), interval: 1m
    """.trimIndent()

    private val succeededPollJson = """
        {
          "state": "SUCCEEDED",
          "progress": 100,
          "result": {
            "records": [
              {
                "timeframe": {
                  "start": "2026-07-29T08:00:00.000000000Z",
                  "end":   "2026-07-29T09:00:00.000000000Z"
                },
                "interval": 60000000000,
                "k8s.pod.name": "blobcachebench-abc",
                "avg(canceled)": [0.0, 1.0, 2.0]
              }
            ],
            "types": [],
            "metadata": {}
          }
        }
    """.trimIndent()

    @BeforeEach
    fun resetWireMock() {
        WireMockTestResource.wireMockServer.resetAll()
    }

    private fun makeDynatraceSli(dynatraceUrl: String, query: String) = Sli().also {
        it.name = "dql-sli"
        it.provider = "dynatrace"
        it.query = query
        it.providerConfig = mutableMapOf("dynatraceUrl" to dynatraceUrl)
    }

    /** Stubs the full OAuth -> execute -> poll flow on the WireMock server. */
    private fun stubDynatraceFlow(requestToken: String = "test-request-token") {
        val server = WireMockTestResource.wireMockServer
        server.stubFor(
            post(urlPathEqualTo("/sso/oauth2/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{"scope":"storage:logs:read","token_type":"Bearer","expires_in":300,""" +
                                """"access_token":"test-access-token","resource":"urn:dtaccount:test"}"""
                        )
                )
        )
        server.stubFor(
            post(urlPathEqualTo("$queryPath:execute"))
                .willReturn(
                    aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"state":"RUNNING","requestToken":"$requestToken","ttlSeconds":399}""")
                )
        )
        server.stubFor(
            get(urlPathEqualTo("$queryPath:poll"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(succeededPollJson)
                )
        )
    }

    @Test
    fun testDynatraceMetricFetcherReturnsMetricQueryResponse() {
        stubDynatraceFlow()
        val queryUrl = "${WireMockTestResource.wireMockServer.baseUrl()}$queryPath"
        val sli = makeDynatraceSli(queryUrl, multilineDqlQuery)
        val fetcher = DynatraceMetricFetcher(sli)

        val response = fetcher.fetchMetric(
            start = exampleDateTime.minus(Duration.ofMinutes(10)),
            end = exampleDateTime,
            stepSize = Duration.ofSeconds(60),
            query = sli.query
        )

        assertInstanceOf(MetricQueryResponse::class.java, response)
        assertFalse(response.isNullOrEmpty())
    }

    /**
     * Regression test for the multi-line DQL query bug: the execute-request body must be valid JSON
     * whose `query` field equals the original query verbatim. With the previous naive string
     * interpolation, the body contained unescaped newlines and quotes, making it invalid JSON that
     * Dynatrace rejected with HTTP 400.
     */
    @Test
    fun testDynatraceExecuteBodyIsValidJsonForMultilineQuery() {
        stubDynatraceFlow()
        val queryUrl = "${WireMockTestResource.wireMockServer.baseUrl()}$queryPath"
        val sli = makeDynatraceSli(queryUrl, multilineDqlQuery)

        DynatraceMetricFetcher(sli).fetchMetric(
            start = exampleDateTime.minus(Duration.ofMinutes(10)),
            end = exampleDateTime,
            stepSize = Duration.ofSeconds(60),
            query = sli.query
        )

        val requests = WireMockTestResource.wireMockServer.findAll(
            postRequestedFor(urlPathEqualTo("$queryPath:execute"))
        )
        assertEquals(1, requests.size)

        // Parsing throws if the body is not valid JSON (as produced by the old interpolation).
        val body = ObjectMapper().readTree(requests.first().bodyAsString)
        assertEquals(multilineDqlQuery, body.get("query").asText())
        assertFalse(body.get("defaultTimeframeStart").asText().isEmpty())
        assertFalse(body.get("defaultTimeframeEnd").asText().isEmpty())
    }
}
