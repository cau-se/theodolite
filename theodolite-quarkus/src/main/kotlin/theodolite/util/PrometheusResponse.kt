package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Description of a prometheus response.
 *
 * @see PromData
 */
@RegisterForReflection
data class PrometheusResponse(
    var status: String? = null,
    var data: PromData? = null
)

/**
 * Description of prometheus data.
 *
 * @see PromResult
 */
@RegisterForReflection
data class PromData(
    var resultType: String? = null,
    var result: List<PromResult>? = null
)

/**
 * Description of a prometheus result.
 *
 * @see PromMetric
 */
@RegisterForReflection
data class PromResult(
    var metric: PromMetric? = null,
    var values: List<Any>? = null
)

/**
 * Description of a query in PromQL.
 */
@RegisterForReflection
data class PromMetric(
    var group: String? = null
)
