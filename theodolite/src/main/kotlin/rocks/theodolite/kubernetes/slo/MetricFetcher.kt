package rocks.theodolite.kubernetes.slo

import java.time.Duration
import java.time.Instant

/**
 * Abstraction for fetching metrics from a specific provider.
 * Each provider implementation owns its own configuration, transport, and retry logic.
 */
interface MetricFetcher {
    /**
     * Fetches a metric for the given time interval.
     *
     * @param start start of the measurement interval.
     * @param end end of the measurement interval.
     * @param stepSize resolution step size (may be ignored by providers that use fixed resolution).
     * @param query provider-specific query string (PromQL, DQL, etc.).
     * @return the collected [MetricQueryResponse].
     * @throws java.net.ConnectException if the provider cannot be reached.
     * @throws NoSuchFieldException if the query yields an empty result.
     */
    fun fetchMetric(start: Instant, end: Instant, stepSize: Duration, query: String): MetricQueryResponse
}
