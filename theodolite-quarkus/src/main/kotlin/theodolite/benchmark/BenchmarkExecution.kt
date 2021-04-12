package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import kotlin.properties.Delegates

/**
 * This Class represents the configuration for an Execution of a Benchmark.
 * An example for this is the BenchmarkExecution.yaml
 * A BenchmarkExecution consists of:
 *  - A [name].
 *  - The [benchmark] (a BenchmarkType) that should be executed.
 *  - The [laod] that should be checked in the benchmark.
 *  - The [resources] that should be checked in the benchmark.
 *  - A List of [slos] that are used for the evaluation of the experiments
 *  - An [execution] that encapsulates: the [strategy], the [duration], the [repetitions], and the [restrictions]
 *  for the execution of the benchmark.
 *  - [configOverrides] additional configurations.
 *  This class is used for the parsing(in the [TheodoliteYamlExecutor]) and
 *  for the deserializing in the [TheodoliteOperator].
 *  @constructor construct an empty BenchmarkExecution.
 */
@JsonDeserialize
@RegisterForReflection
class BenchmarkExecution : CustomResource(), Namespaced {
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var load: LoadDefinition
    lateinit var resources: ResourceDefinition
    lateinit var slos: List<Slo>
    lateinit var execution: Execution
    lateinit var configOverrides: List<ConfigurationOverride?>

    /**
     * This execution encapsulates the [strategy], the [duration], the [repetitions], and the [restrictions]
     *  which are used for the concrete benchmark experiments.
     */
    @JsonDeserialize
    @RegisterForReflection
    class Execution : KubernetesResource {
        lateinit var strategy: String
        var duration by Delegates.notNull<Long>()
        var repetitions by Delegates.notNull<Int>()
        lateinit var restrictions: List<String>
    }

    /**
     * Measurable Metric.
     * It is evaluated using the [ExternalSloChecker] by data measured by Prometheus.
     * The evaluation checks if a [threshold] is reached or not.
     * It has a [offset] by which the start and end points of the Metric can be shifted.
     * The [warmup] determines after which time the metric should be evaluated to avoid starting interferences.
     */
    @JsonDeserialize
    @RegisterForReflection
    class Slo : KubernetesResource {
        lateinit var sloType: String
        var threshold by Delegates.notNull<Int>()
        lateinit var prometheusUrl: String
        lateinit var externalSloUrl: String
        var offset by Delegates.notNull<Int>()
        var warmup by Delegates.notNull<Int>()
    }

    /**
     * Represents a Load that should be created and checked.
     * It can be set to [loadValues].
     */
    @JsonDeserialize
    @RegisterForReflection
    class LoadDefinition : KubernetesResource {
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    /**
     * Represents a resource that can be scaled to [resourceValues].
     */
    @JsonDeserialize
    @RegisterForReflection
    class ResourceDefinition : KubernetesResource {
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
