package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * This class corresponds to the JSON response format of a Prometheus
 * [range-query](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-queries)
 */
@RegisterForReflection
data class PrometheusResponse(
    /**
     * Indicates whether the query was successful.
     */
    var status: String? = null,
    /**
     * The data section of the query result contains the information about the resultType and the values itself.
     */
    var data: PromData? = null
)

/**
 * Description of Prometheus data.
 *
 * Based on [PromResult]
 */
@RegisterForReflection
data class PromData(
    /**
     * Type of the result, either  "matrix" | "vector" | "scalar" | "string"
     */
    var resultType: String? = null,
    /**
     * Result of the range-query. In the case of range-query this corresponds to the [range-vectors result format](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-vectors)
     */
    var result: List<PromResult>? = null
)

/**
 * PromResult corresponds to the [range-vectors result format](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-vectors)
 */
@RegisterForReflection
data class PromResult(
    /**
     * Label of the metric
     */
    var metric: PromMetric? = null,
    /**
     *  Values of the metric (e.g. [ [ <unix_time>, "<sample_value>" ], ... ])
     */
    var values: List<Any>? = null
)

/**
 * Corresponds to the metric field in the range-vector result format of a Prometheus range-query response.
 */
@RegisterForReflection
data class PromMetric(
    var group: String? = null
)