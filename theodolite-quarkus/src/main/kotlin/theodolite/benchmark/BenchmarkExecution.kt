package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import theodolite.util.ConfigurationOverride
import java.lang.System.exit
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@JsonDeserialize
class BenchmarkExecution : CustomResource(), Namespaced {
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var load: LoadDefinition
    lateinit var resources: ResourceDefinition
    lateinit var slos: List<Slo>
    lateinit var execution: Execution
    lateinit var configOverrides: List<ConfigurationOverride?>

    fun stop() {
        throw InterruptedException()
    }

    @JsonDeserialize
    class Execution : KubernetesResource {
        lateinit var strategy: String
        var duration by Delegates.notNull<Long>()
        var repetitions by Delegates.notNull<Int>()
        lateinit var restrictions: List<String>
    }

    @JsonDeserialize
    class Slo : KubernetesResource{
        lateinit var sloType: String
        var threshold by Delegates.notNull<Int>()
        lateinit var prometheusUrl: String
        lateinit var externalSloUrl: String
        var offset by Delegates.notNull<Int>()
        var warmup by Delegates.notNull<Int>()
    }

    @JsonDeserialize
    class LoadDefinition : KubernetesResource{
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    @JsonDeserialize
    class ResourceDefinition : KubernetesResource{
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
