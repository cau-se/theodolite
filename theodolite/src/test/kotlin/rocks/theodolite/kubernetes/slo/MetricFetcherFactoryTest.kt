package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli

@QuarkusTest
internal class MetricFetcherFactoryTest {

    @Test
    fun testMetricFetcherFactoryCreatesPrometheusFetcher() {
        val sli = Sli().also {
            it.name = "test-sli"
            it.provider = "prometheus"
            it.query = "sum by(consumergroup) (kafka_consumergroup_lag >= 0)"
            it.providerConfig = mutableMapOf("prometheusUrl" to "http://localhost:9090")
        }
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
