package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli
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
            return emptyMap()
        }

        override fun stop() {
            wireMockServer.stop()
        }
    }

    private val exampleDateTime = LocalDateTime.of(2023, 7, 24, 10, 22, 0).toInstant(ZoneOffset.UTC)

    private fun makeSli(prometheusUrl: String) = Sli().also {
        it.name = "test-sli"
        it.provider = "prometheus"
        it.query = "sum by(consumergroup) (kafka_consumergroup_lag >= 0)"
        it.providerConfig = mutableMapOf("prometheusUrl" to prometheusUrl)
    }

    @Test
    fun testPrometheusMetricFetcherReturnsMetricQueryResponse() {
        val prometheusResponse = PrometheusResponse(
            data = PromData(result = listOf(PromResult()))
        )
        WireMockTestResource.wireMockServer.stubFor(
            get(urlPathEqualTo("/api/v1/query_range"))
                .willReturn(aResponse().withJsonBody(ObjectMapper().valueToTree(prometheusResponse)))
        )

        val sli = makeSli(WireMockTestResource.wireMockServer.baseUrl())
        val fetcher = PrometheusMetricFetcher(sli)
        val response = fetcher.fetchMetric(
            start = exampleDateTime.minus(Duration.ofMinutes(10)),
            end = exampleDateTime,
            stepSize = Duration.ofSeconds(5),
            query = sli.query
        )

        assertInstanceOf(MetricQueryResponse::class.java, response)
        assertFalse(response.isNullOrEmpty())
    }

    @Test
    fun testMetricFetcherFactoryCreatesPrometheusFetcher() {
        val sli = makeSli("http://localhost:9090")
        val fetcher = MetricFetcherFactory.create(sli)
        assertInstanceOf(PrometheusMetricFetcher::class.java, fetcher)
    }

    @Test
    fun testMetricFetcherFactoryCreatesDynatraceFetcher() {
        val sli = Sli().also {
            it.name = "dql-sli"
            it.provider = "dynatrace"
            it.query = "timeseries avg(dt.host.cpu.usage)"
            it.providerConfig = mutableMapOf("dynatraceUrl" to "https://example.dynatrace.com/api/v2/query")
        }
        val fetcher = MetricFetcherFactory.create(sli)
        assertInstanceOf(DynatraceMetricFetcher::class.java, fetcher)
    }
}
