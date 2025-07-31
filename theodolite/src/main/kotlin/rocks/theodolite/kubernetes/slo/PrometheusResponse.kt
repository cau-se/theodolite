package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * This class corresponds to the JSON response format of a Prometheus
 * [range-query](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-queries)
 */
@RegisterForReflection
data class PrometheusResponse (
    /**
     * Indicates whether the query was successful.
     */
    var status: String? = null,
    /**
     * The data section of the query result contains the information about the resultType and the values itself.
     */
    var data: PromData? = null
) : MetricQueryResponse {
    companion object : MetricQueryResponseFactory<MetricQueryResponse> {
        override fun fromString(json: String): PrometheusResponse{
            return ObjectMapper().readValue(
                json,
                PrometheusResponse::class.java
            )
        }
    }

    @JsonIgnore
    override fun isNullOrEmpty(): Boolean {
        return data?.result.isNullOrEmpty()
    }

    @JsonIgnore
    override fun getDataForSLOChecker(): List<SloJson.MetricResult> {
        return this.data?.result?.map { promResult ->
            SloJson.MetricResult(
                metric = promResult.metric,
                values = promResult.values
            )
        } ?: emptyList()
    }

    @JsonIgnore
    override fun getResultAsList(onlyFirst: Boolean): List<List<String>> {
        val resultsList = mutableListOf<List<String>>()
        val results = data?.result
        check(results != null) {"No 'results' available in the Prometheus response."}

        for (result in results.subList(0, if (onlyFirst && results.isNotEmpty()) 1 else results.size)) {
            val group = result.metric.toString()
            val values = result.values

            if (values != null) {
                for (value in values) {
                    val valueList = value as List<*>
                    val timestamp = (valueList[0] as Number).toLong().toString()
                    val resultValue = valueList[1].toString()
                    resultsList.add(listOf(group, timestamp, resultValue))
                }
            }
        }
        return resultsList.toList()
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
    var metric: Map<String, String>? = null,
    /**
     *  Values of the metric (e.g. [ [ <unix_time>, "<sample_value>" ], ... ])
     */
    var values: List<Any>? = null
)

