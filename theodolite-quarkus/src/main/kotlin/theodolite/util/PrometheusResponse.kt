package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class PrometheusResponse(
    var status: String? = null,
    var data: PromData? = null
)

@RegisterForReflection
data class PromData(
    var resultType: String? = null,
    var result: List<PromResult>? = null
)

@RegisterForReflection
data class PromResult(
    var metric: PromMetric? = null,
    var values: List<Any>? = null
)

@RegisterForReflection
data class PromMetric(
    var group: String? = null
)
