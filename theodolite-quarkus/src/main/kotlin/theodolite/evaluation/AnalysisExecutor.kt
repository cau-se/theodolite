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

    fun analyze(load: LoadDimension, res: Resource, executionIntervals: List<Pair<Instant, Instant>>): Boolean {
        var result = false
        val exporter = CsvExporter()
        val prometheusData = executionIntervals.map { interval -> fetcher.fetchMetric( start = interval.first, end = interval.second, query = "sum by(group)(kafka_consumergroup_group_lag >= 0)") }
        var repetitionCounter = 1
        prometheusData.forEach{ data -> exporter.toCsv(name = "${load.get()}_${res.get()}_${slo.sloType}_rep_${repetitionCounter++}", prom = data) }
        prometheusData.forEach { logger.info { "prom-data: $it" }}

        try {

            val sloChecker = SloCheckerFactory().create(
                slotype = slo.sloType,
                externalSlopeURL = slo.externalSloUrl,
                threshold = slo.threshold,
                warmup = slo.warmup
            )

            result = sloChecker.evaluate(prometheusData)

        } catch (e: Exception) {
            logger.error { "Evaluation failed for resource: ${res.get()} and load: ${load.get()} error: $e" }
        }
        return result
    }
}
