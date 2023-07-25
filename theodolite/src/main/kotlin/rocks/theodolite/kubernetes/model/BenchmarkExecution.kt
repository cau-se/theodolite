package rocks.theodolite.kubernetes.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import kotlin.properties.Delegates

/**
 * This class represents the configuration for an execution of a benchmark.
 * An example for this is the BenchmarkExecution.yaml
 * A BenchmarkExecution consists of:
 *  - A [name].
 *  - The [benchmark] that should be executed.
 *  - The [load]s that should be checked in the benchmark.
 *  - The [resources] that should be checked in the benchmark.
 *  - The [slos] further restrict the Benchmark SLOs for the evaluation of the experiments.
 *  - An [execution] that encapsulates: the strategy, the duration, and the restrictions
 *  for the execution of the benchmark.
 *  - [configOverrides] additional configurations.
 *  This class is used for parsing(in [theodolite.execution.TheodoliteStandalone]) and
 *  for the deserializing in the [theodolite.execution.operator.TheodoliteOperator].
 *  @constructor construct an empty BenchmarkExecution.
 */
@JsonDeserialize
@RegisterForReflection
class BenchmarkExecution : KubernetesResource {
    var executionId: Int = 0
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var load: LoadDefinition
    lateinit var resources: ResourceDefinition
    var slos: List<SloConfiguration>? = null
    lateinit var execution: Execution
    lateinit var configOverrides: MutableList<ConfigurationOverride?>

    /**
     * This execution encapsulates the [strategy], the [duration], and the [repetitions],
     *  which are used for the concrete benchmark experiments.
     */
    @JsonDeserialize
    @RegisterForReflection
    class Execution : KubernetesResource {
        var metric = "demand"
        lateinit var strategy: Strategy
        var duration by Delegates.notNull<Long>()
        var repetitions by Delegates.notNull<Int>()
        var loadGenerationDelay = 0L
        var afterTeardownDelay = 5L
    }

    /**
     * This Strategy encapsulates the [restrictions], [guessStrategy] and [searchStrategy],
     * which are used for restricting the resources, the guess Strategy for the
     * [theodolite.strategies.searchstrategy.InitialGuessSearchStrategy] and the name of the actual
     * [theodolite.strategies.searchstrategy.SearchStrategy] which is used.
     */
    @JsonDeserialize
    @RegisterForReflection
    class Strategy : KubernetesResource {
        lateinit var name: String
        var restrictions = emptyList<String>()
        var guessStrategy = ""
        var searchStrategy = ""
    }

    /**
     * Further SLO configurations for the SLOs specified in the Benchmark.
     */
    @JsonDeserialize
    @RegisterForReflection
    class SloConfiguration : KubernetesResource {
        lateinit var name: String
        var properties: MutableMap<String, String>? = null
    }

    /**
     * Represents the Loads that should be created and checked if the demand metric is in use or
     * represents a Load that can be scaled to [loadValues] if the capacity metric is in use.
     */
    @JsonDeserialize
    @RegisterForReflection
    class LoadDefinition : KubernetesResource {
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    /**
     * Represents a resource that can be scaled to [resourceValues] if the demand metric is in use or
     * represents the Resources that should be created and checked if the capacity metric is in use.
     */
    @JsonDeserialize
    @RegisterForReflection
    class ResourceDefinition : KubernetesResource {
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
