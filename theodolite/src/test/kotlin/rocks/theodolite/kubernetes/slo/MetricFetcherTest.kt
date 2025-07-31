package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@QuarkusTest
@QuarkusTestResource(MetricFetcherTest.WireMockTestResource::class)
internal class MetricFetcherTest {
    internal class WireMockTestResource : QuarkusTestResourceLifecycleManager {
        companion object {
            lateinit var wireMockServer: WireMockServer private set
        }

        override fun start(): Map<String, String> {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()
            return mapOf("query.url" to wireMockServer.baseUrl(), "time.offset.ms" to "0")
        }

        override fun stop() {
            wireMockServer.stop()
        }
    }


    private val exampleDateTime = LocalDateTime.of(2023, 7, 24, 10, 22, 0).toInstant(ZoneOffset.UTC)

    @Test
    fun testRealPromQlQuery() {
        val emptyPrometheusResponse = PrometheusResponse(
                data = PromData(
                        result = listOf(
                                PromResult()
                        )
                )
        )
        WireMockTestResource.wireMockServer.stubFor(
            get(urlPathEqualTo("/api/v1/query_range"))
                .willReturn(
                    aResponse().withJsonBody(
                            ObjectMapper().valueToTree(emptyPrometheusResponse))
                )
        )

        val metricFetcher = MetricFetcher()
        val response = metricFetcher.fetchMetric(
                exampleDateTime.minus(Duration.ofMinutes(10)),
                exampleDateTime,
                Duration.ofSeconds(5),
                "sum by(consumergroup) (kafka_consumergroup_lag >= 0)",
                MetricFetcher.Kind.PROMETHEUS)


        assertEquals(emptyPrometheusResponse, response)
    }

}
