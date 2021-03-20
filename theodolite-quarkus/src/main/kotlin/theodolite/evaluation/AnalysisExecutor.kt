package theodolite.evaluation

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.util.LoadDimension
import theodolite.util.PrometheusResponse
import theodolite.util.Resource
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Executes the Analysis.
 */
class AnalysisExecutor {

    fun analyse(load:LoadDimension,executionDuration: Duration, res: Resource,slo: BenchmarkExecution.Slo): Boolean {
        var result = false

        try {

            val prometheusData = fetch(Instant.now().minus(executionDuration),
                Instant.now(),
                slo,
                "sum by(group)(kafka_consumergroup_group_lag >= 0)")

            CsvExporter().toCsv("${load.get()}_${res.get()}_${slo.sloType}",prometheusData)

            val sloChecker = SloCheckerFactory().create(
                slotype = slo.sloType,
                prometheusURL = slo.prometheusUrl,
                query = "sum by(group)(kafka_consumergroup_group_lag >= 0)",
                externalSlopeURL = slo.externalSloUrl,
                threshold = slo.threshold,
                offset = Duration.ofHours(slo.offset.toLong()),
                warmup = slo.warmup
            )

           result =  sloChecker.evaluate(start = Instant.now().minus(executionDuration),
                end = Instant.now(),fetchedData = prometheusData)

        } catch (e: Exception) {
            logger.error { "Evaluation failed for resource: ${res.get()} and load: ${load.get()} error: $e" }
        }

        return result
    }

    fun fetch(start: Instant, end: Instant,slo: BenchmarkExecution.Slo,query: String): PrometheusResponse {

        val metricFetcher = MetricFetcher(prometheusURL = slo.prometheusUrl,
            offset = Duration.ofHours(slo.offset.toLong()))
        val fetchedData = metricFetcher.fetchMetric(start, end, query)

        return fetchedData
    }
}
