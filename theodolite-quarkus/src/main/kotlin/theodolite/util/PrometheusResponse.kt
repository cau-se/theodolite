package theodolite.util

data class PrometheusResponse(
    var status: String? = null,
    var data: PromData? = null
)

data class PromData(
    var resultType: String? = null,
    var result: List<PromResult>? = null
)

data class PromResult(
    var metric: PromMetric? = null,
    var values: List<Object>? = null
)

data class PromMetric(
    var group: String? = null
)
