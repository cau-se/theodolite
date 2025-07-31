package rocks.theodolite.kubernetes.slo

import org.eclipse.microprofile.config.ConfigProvider
import java.net.ConnectException
import java.net.URI
import java.time.Duration
import java.time.Instant


/**
 * Used to fetch metrics from Prometheus or Dynatrace.
 */
class MetricFetcher {
    private val queryURI: URI = ConfigProvider.getConfig().getValue("query.url", URI::class.java)
    private val offset: Duration = ConfigProvider.getConfig().getValue("time.offset.ms", Duration::class.java)

    enum class Kind {
        PROMETHEUS, DYNATRACE
    }


    private val TIMEOUT = Duration.ofSeconds(60)


    /**
     * Tries to fetch a metric by a query to a Prometheus server or DQL.
     * Retries to fetch the metric [RETRIES] times.
     * Connects to the server via [queryURI].
     *
     * @param start start point of the query.
     * @param end end point of the query.
     * @param query query for the server.
     * @throws ConnectException - if the server timed out/was not reached.
     */
    fun fetchMetric(start: Instant, end: Instant, stepSize: Duration, query: String, kind: Kind): MetricQueryResponse {
        val offsetStart = start.minus(offset)
        val offsetEnd = end.minus(offset)

        val executor = MetricRequestExecutorFactory.get(kind)

        val queryResponse = executor.executeRequest(queryURI, query, offsetStart, offsetEnd, stepSize, TIMEOUT)

        if (queryResponse.isNullOrEmpty()) {
            throw NoSuchFieldException(
                "Empty query result: $queryResponse between for query '$query' in interval [$offsetStart,$offsetEnd] .")
        }
        return queryResponse
    }


}
