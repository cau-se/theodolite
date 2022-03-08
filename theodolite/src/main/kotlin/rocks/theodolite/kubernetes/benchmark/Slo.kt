package rocks.theodolite.kubernetes.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection
import kotlin.properties.Delegates

/**
 * Measurable metric.
 * [sloType] determines the type of the metric.
 * It is evaluated using the [theodolite.evaluation.ExternalSloChecker] by data measured by Prometheus.
 * The evaluation checks if a [threshold] is reached or not.
 * [offset] determines the shift in hours by which the start and end timestamps should be shifted.
 * The [warmup] determines after which time the metric should be evaluated to avoid starting interferences.
 * The [warmup] time unit depends on the Slo: for the lag trend it is in seconds.
 */
@JsonDeserialize
@RegisterForReflection
class Slo : KubernetesResource {
    lateinit var name: String
    lateinit var sloType: String
    lateinit var prometheusUrl: String
    var offset by Delegates.notNull<Int>()
    lateinit var properties: MutableMap<String, String>
}