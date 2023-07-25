package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.BooleanNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@QuarkusTest
internal class MetricFetcherTest {

    private var wireMockServer: WireMockServer? = null

    private val exampleDateTime = LocalDateTime.of(2023, 7, 24, 10, 22, 0).toInstant(ZoneOffset.UTC)

    @BeforeEach
    fun start() {
        wireMockServer = WireMockServer().also {
            it.start()
        }
    }

    @AfterEach
    fun stop() {
        wireMockServer?.stop()
    }

    @Test
    fun testRealPromQlQuery() {
        val emptyPrometheusResponse = PrometheusResponse(
                data = PromData(
                        result = listOf(
                                PromResult()
                        )
                )
        )
        this.wireMockServer!!.stubFor(
            get(urlPathEqualTo("/api/v1/query_range"))
                .willReturn(
                    aResponse().withJsonBody(
                            ObjectMapper().valueToTree(emptyPrometheusResponse))
                )
        )

        val metricFetcher = MetricFetcher(wireMockServer!!.baseUrl(), Duration.ZERO)
        val response = metricFetcher.fetchMetric(
                exampleDateTime.minus(Duration.ofMinutes(10)),
                exampleDateTime,
                "sum by(consumergroup) (kafka_consumergroup_lag >= 0)")

        assertEquals(emptyPrometheusResponse, response)
    }

}