package rocks.theodolite.kubernetes.slo.evaluation

import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.util.IOHandler
import rocks.theodolite.kubernetes.benchmark.Slo
import rocks.theodolite.kubernetes.util.exception.EvaluationFailedException
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

/**
 * Contains the analysis. Fetches a metric from Prometheus, documents it, and evaluates it.
 * @param slo Slo that is used for the analysis.
 */
class AnalysisExecutor(
        private val slo: Slo,
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
     *  @param resource of the experiment.
     *  @param executionIntervals list of start and end points of experiments
     *  @return true if the experiment succeeded.
     */
    fun analyze(load: Int, resource: Int, executionIntervals: List<Pair<Instant, Instant>>, metric: Metric): Boolean {
        var repetitionCounter = 1

        try {
            val ioHandler = IOHandler()
            val resultsFolder: String = ioHandler.getResultFolderURL()
            val fileURL = "${resultsFolder}exp${executionId}_${load}_${resource}_${slo.sloType.toSlug()}"

            val prometheusData = executionIntervals
                .map { interval ->
                    fetcher.fetchMetric(
                        start = interval.first,
                        end = interval.second,
                        query = SloConfigHandler.getQueryString(slo = slo)
                    )
                }

            prometheusData.forEach { data ->
                ioHandler.writeToCSVFile(
                    fileURL = "${fileURL}_${repetitionCounter++}",
                    data = data.getResultAsList(),
                    columns = listOf("labels", "timestamp", "value")
                )
            }

            val sloChecker = SloCheckerFactory().create(
                sloType = slo.sloType,
                properties = slo.properties,
                load = load,
                resource = resource,
                metric = metric
            )

            return sloChecker.evaluate(prometheusData)

        } catch (e: Exception) {
            throw EvaluationFailedException("Evaluation failed for resource '$resource' and load '$load ", e)
        }
    }

    private val NONLATIN: Pattern = Pattern.compile("[^\\w-]")
    private val WHITESPACE: Pattern = Pattern.compile("[\\s]")

    private fun String.toSlug(): String {
        val noWhitespace: String = WHITESPACE.matcher(this).replaceAll("-")
        val normalized: String = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD)
        val slug: String = NONLATIN.matcher(normalized).replaceAll("")
        return slug.lowercase(Locale.ENGLISH)
    }
}
