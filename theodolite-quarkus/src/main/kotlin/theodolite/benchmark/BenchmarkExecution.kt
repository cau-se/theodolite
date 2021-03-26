package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import kotlin.properties.Delegates

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

    @JsonDeserialize
    @RegisterForReflection
    class Execution : KubernetesResource {
        lateinit var strategy: String
        var duration by Delegates.notNull<Long>()
        var repetitions by Delegates.notNull<Int>()
        lateinit var restrictions: List<String>
    }

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


    @JsonDeserialize
    @RegisterForReflection
    class LoadDefinition : KubernetesResource {
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    @JsonDeserialize
    @RegisterForReflection
    class ResourceDefinition : KubernetesResource {
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
