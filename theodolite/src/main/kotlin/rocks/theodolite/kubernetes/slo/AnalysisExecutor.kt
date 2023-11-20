package rocks.theodolite.kubernetes.slo

import rocks.theodolite.core.IOHandler
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

private val DEFAULT_STEP_SIZE = Duration.ofSeconds(5)

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
    fun analyze(load: Int, resource: Int, executionIntervals: List<Pair<Instant, Instant>>): Boolean {
        var repetitionCounter = 1

        try {
            val ioHandler = IOHandler()
            val resultsFolder = ioHandler.getResultFolderURL()
            val fileURL = "${resultsFolder}exp${executionId}_${load}_${resource}_${slo.sloType.toSlug()}"

            val stepSize = slo.properties["promQLStepSeconds"]?.toLong()?.let { Duration.ofSeconds(it) } ?: DEFAULT_STEP_SIZE

            val prometheusData = executionIntervals
                .map { interval ->
                    fetcher.fetchMetric(
                        start = interval.first,
                        end = interval.second,
                        stepSize = stepSize,
                        query = SloConfigHandler.getQueryString(slo = slo)
                    )
                }

            prometheusData.forEach{ data ->
                ioHandler.writeToCSVFile(
                    fileURL = "${fileURL}_${slo.name}_${repetitionCounter++}",
                    data = data.getResultAsList(),
                    columns = listOf("labels", "timestamp", "value")
                )
            }

            val sloChecker = SloCheckerFactory().create(
                sloType = slo.sloType,
                properties = slo.properties,
                load = load,
                resources = resource
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
