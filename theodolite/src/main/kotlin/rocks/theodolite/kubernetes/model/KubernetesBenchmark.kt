package rocks.theodolite.kubernetes.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.Action
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.patcher.PatcherDefinition

/**
 * Represents a benchmark in Kubernetes. An example for this is the BenchmarkType.yaml
 * Contains a of:
 * - [name] of the benchmark,
 * - [appResource] list of the resources that have to be deployed for the benchmark,
 * - [loadGenResource] resource that generates the load,
 * - [resourceTypes] types of scaling resources,
 * - [loadTypes] types of loads that can be scaled for the benchmark,
 * - [namespace] for the client,
 * - [path] under which the resource yamls can be found.
 *
 * @constructor construct an empty Benchmark.
 */
@JsonDeserialize
@RegisterForReflection
class KubernetesBenchmark : KubernetesResource {
    lateinit var name: String
    var waitForResourcesEnabled = false
    lateinit var resourceTypes: List<TypeName>
    lateinit var loadTypes: List<TypeName>
    lateinit var slis: MutableList<Sli>
    lateinit var slos: MutableList<Slo>
    lateinit var infrastructure: Resources
    lateinit var sut: Resources
    lateinit var loadGenerator: Resources

    /**
     * The TypeName encapsulates a list of [PatcherDefinition] along with a typeName that specifies for what the [PatcherDefinition] should be used.
     */
    @RegisterForReflection
    @JsonDeserialize
    class TypeName {
        lateinit var typeName: String
        lateinit var patchers: List<PatcherDefinition>
    }

    /**
     * A Service Level Indicator (SLI) defines a named, provider-based metric query.
     * Results are always exported to CSV.
     * [name] is the unique identifier used to reference this SLI from SLOs.
     * [provider] specifies the metric source (currently only "prometheus").
     * [query] is the PromQL query string.
     * [intervalSeconds] is the Prometheus step size in seconds (default: 5).
     * [providerConfig] is a loose map for provider-specific settings (e.g., prometheusUrl, offsetHours).
     */
    @JsonDeserialize
    @RegisterForReflection
    class Sli : KubernetesResource {
        lateinit var name: String
        lateinit var provider: String
        lateinit var query: String
        var intervalSeconds: Int? = null
        var providerConfig: MutableMap<String, String> = mutableMapOf()
    }

    /**
     * A Service Level Objective (SLO) references an SLI by name and defines the evaluation rules.
     * [name] is a unique identifier.
     * [sli] must match the name of an SLI defined in the same benchmark.
     * [warmupSeconds] specifies how many seconds at the start of each interval to skip.
     * [queryAggregation] and [repetitionAggregation] determine how values are aggregated.
     * [operator] and [threshold] (or one of the relative/expression variants) define the pass/fail condition.
     * [externalSloChecker] overrides the default generic checker URL.
     */
    @JsonDeserialize
    @RegisterForReflection
    class Slo : KubernetesResource {
        lateinit var name: String
        lateinit var sli: String
        var warmupSeconds: Int = 0
        var queryAggregation: String? = null
        var repetitionAggregation: String? = null
        var operator: String? = null
        var threshold: Double? = null
        var thresholdRelToLoad: Double? = null
        var thresholdRelToResources: Double? = null
        var thresholdFromExpression: String? = null
        var externalSloChecker: String? = null
    }

    @JsonDeserialize
    @RegisterForReflection
    class Resources {
        lateinit var resources: List<ResourceSets>
        lateinit var beforeActions: List<Action>
        lateinit var afterActions: List<Action>
    }
}
