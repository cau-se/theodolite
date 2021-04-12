package theodolite.evaluation

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.util.LoadDimension
import theodolite.util.Resource
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

class AnalysisExecutor(private val slo: BenchmarkExecution.Slo) {

    private val fetcher = MetricFetcher(
        prometheusURL = slo.prometheusUrl,
        offset = Duration.ofHours(slo.offset.toLong())
    )

    fun analyze(load: LoadDimension, res: Resource, executionDuration: Duration): Boolean {
        var result = false

        try {
            val prometheusData = fetcher.fetchMetric(
                start = Instant.now().minus(executionDuration),
                end = Instant.now(),
                query = "sum by(group)(kafka_consumergroup_group_lag >= 0)"
            )

            CsvExporter().toCsv(name = "${load.get()}_${res.get()}_${slo.sloType}", prom = prometheusData)

            val sloChecker = SloCheckerFactory().create(
                slotype = slo.sloType,
                externalSlopeURL = slo.externalSloUrl,
                threshold = slo.threshold,
                warmup = slo.warmup
            )

            result = sloChecker.evaluate(
                start = Instant.now().minus(executionDuration),
                end = Instant.now(), fetchedData = prometheusData
            )

        } catch (e: Exception) {
            logger.error { "Evaluation failed for resource: ${res.get()} and load: ${load.get()} error: $e" }
        }
        return result
    }
}
