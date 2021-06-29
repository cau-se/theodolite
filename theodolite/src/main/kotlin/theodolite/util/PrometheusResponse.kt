package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

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
{
    /**
     * Return the data of the PrometheusResponse as [List] of [List]s of [String]s
     * The format of the returned list is: `[[ group, timestamp, value ], [ group, timestamp, value ], ... ]`
     */
    fun getResultAsList(): List<List<String>> {
        val group = data?.result?.get(0)?.metric?.group.toString()
        val values = data?.result?.get(0)?.values
        val result = mutableListOf<List<String>>()

        if (values != null) {
            for (value in values) {
                val valueList = value as List<*>
                val timestamp = (valueList[0] as Double).toLong().toString()
                val value = valueList[1].toString()
                result.add(listOf(group, timestamp, value))
            }
        }
        return Collections.unmodifiableList(result)
    }
}

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

