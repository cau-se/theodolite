package theodolite.evaluation

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.util.LoadDimension
import theodolite.util.Resource
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

/**
 * Contains the analysis. Fetches a metric from Prometheus, documents it, and evaluates it.
 * @param slo Slo that is used for the analysis.
 */
class AnalysisExecutor(
    private val slo: BenchmarkExecution.Slo,
    private val executionId: Int
) {

    private val fetcher = MetricFetcher(
        prometheusURL = slo.prometheusUrl,
        offset = Duration.ofHours(slo.offset.toLong())
    )

    /**
     *  Analyses an experiment via prometheus data.
     *  First fetches data from prometheus, then documents them and afterwards evaluate it via a [slo].
     *  @param load of the experiment.
     *  @param res of the experiment.
     *  @param executionDuration of the experiment.
     *  @return true if the experiment succeeded.
     */
    fun analyze(load: LoadDimension, res: Resource, executionDuration: Duration): Boolean {
        var result = false

        try {
            val prometheusData = fetcher.fetchMetric(
                start = Instant.now().minus(executionDuration),
                end = Instant.now(),
                query = "sum by(group)(kafka_consumergroup_group_lag >= 0)"
            )

            CsvExporter().toCsv(
                name = "exp${executionId}_${load.get()}_${res.get()}_${slo.sloType.toSlug()}",
                prom = prometheusData
            )
            val sloChecker = SloCheckerFactory().create(
                sloType = slo.sloType,
                externalSlopeURL = slo.externalSloUrl,
                threshold = slo.threshold,
                warmup = slo.warmup
            )

            result = sloChecker.evaluate(
                start = Instant.now().minus(executionDuration),
                end = Instant.now(), fetchedData = prometheusData
            )

        } catch (e: Exception) {
            logger.error { "Evaluation failed for resource '${res.get()}' and load '${load.get()}'. Error: $e" }
        }
        return result
    }

    private val NONLATIN: Pattern = Pattern.compile("[^\\w-]")
    private val WHITESPACE: Pattern = Pattern.compile("[\\s]")

    fun String.toSlug(): String {
        val noWhitespace: String = WHITESPACE.matcher(this).replaceAll("-")
        val normalized: String = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD)
        val slug: String = NONLATIN.matcher(normalized).replaceAll("")
        return slug.toLowerCase(Locale.ENGLISH)
    }
}
