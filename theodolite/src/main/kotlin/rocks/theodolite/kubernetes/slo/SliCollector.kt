package rocks.theodolite.kubernetes.slo

import mu.KotlinLogging
import rocks.theodolite.core.IOHandler
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}
private val DEFAULT_INTERVAL_SECONDS = 5

/**
 * Collects metrics for a set of SLIs across a list of experiment intervals.
 * All collected data is exported to CSV; results are returned for SLO evaluation.
 *
 * @param slis List of resolved SLIs to collect.
 * @param executionId Used for CSV file naming.
 */
class SliCollector(
    private val slis: List<Sli>,
    private val executionId: Int
) {
    private val ioHandler = IOHandler()
    private val resultsFolder = ioHandler.getResultFolderURL()

    /**
     * Collects data for all SLIs over the given [executionIntervals].
     * Writes full (all-labels) CSV per SLI per repetition.
     *
     * @return Map from SLI name to its list of [MetricQueryResponse]s (one per interval/repetition).
     */
    fun collect(
        load: Int,
        resource: Int,
        executionIntervals: List<Pair<Instant, Instant>>
    ): Map<String, List<MetricQueryResponse>> {
        return slis.associate { sli ->
            val stepSize = Duration.ofSeconds((sli.intervalSeconds ?: DEFAULT_INTERVAL_SECONDS).toLong())
            val fetcher = MetricFetcherFactory.create(sli)

            logger.info { "Collecting SLI '${sli.name}' (provider: ${sli.provider}) with query '${sli.query}'" }

            val data = executionIntervals.map { interval ->
                fetcher.fetchMetric(
                    start = interval.first,
                    end = interval.second,
                    stepSize = stepSize,
                    query = sli.query
                )
            }

            val fileBase = "${resultsFolder}exp${executionId}_${load}_${resource}_sli_${sli.name.toSlug()}"
            data.forEachIndexed { index, response ->
                ioHandler.writeToCSVFile(
                    fileURL = "${fileBase}_${index + 1}",
                    data = response.getResultAsList(onlyFirst = false),
                    columns = listOf("labels", "timestamp", "value")
                )
            }

            sli.name to data
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
