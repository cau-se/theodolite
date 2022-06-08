package rocks.theodolite.kubernetes.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.Action
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.model.crd.KafkaConfig
import rocks.theodolite.kubernetes.patcher.PatcherDefinition
import kotlin.properties.Delegates

/**
 * Represents a benchmark in Kubernetes. An example for this is the BenchmarkType.yaml
 * Contains a of:
 * - [name] of the benchmark,
 * - [appResource] list of the resources that have to be deployed for the benchmark,
 * - [loadGenResource] resource that generates the load,
 * - [resourceTypes] types of scaling resources,
 * - [loadTypes] types of loads that can be scaled for the benchmark,
 * - [kafkaConfig] for the [theodolite.k8s.TopicManager],
 * - [namespace] for the client,
 * - [path] under which the resource yamls can be found.
 *
 *  This class is used for the parsing(in the [theodolite.execution.TheodoliteStandalone]) and
 *  for the deserializing in the [theodolite.execution.operator.TheodoliteOperator].
 * @constructor construct an empty Benchmark.
 */
@JsonDeserialize
@RegisterForReflection
class KubernetesBenchmark : KubernetesResource {
    lateinit var name: String
    var waitForResourcesEnabled = false
    lateinit var resourceTypes: List<TypeName>
    lateinit var loadTypes: List<TypeName>
    lateinit var slos: MutableList<Slo>
    var kafkaConfig: KafkaConfig? = null
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

    @JsonDeserialize
    @RegisterForReflection
    class Resources {
        lateinit var resources: List<ResourceSets>
        lateinit var beforeActions: List<Action>
        lateinit var afterActions: List<Action>
    }
}
