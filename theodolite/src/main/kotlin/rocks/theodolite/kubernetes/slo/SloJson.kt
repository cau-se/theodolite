package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper


class SloJson constructor(
    val results: List<List<MetricResult>>,
    var metadata: Map<String, Any>
) {
    data class MetricResult(
        /**
         * Label of the metric
         */
        var metric: Map<String, String>? = null,
        /**
         *  Values of the metric (e.g. [ [ <unix_time>, "<sample_value>" ], ... ])
         */
        var values: List<Any?>? = null
    )
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(
            mapOf(
                "results" to this.results,
                "metadata" to this.metadata
            )
        )
    }
}
