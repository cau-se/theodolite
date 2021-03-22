package theodolite.benchmark

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import kotlin.properties.Delegates

@RegisterForReflection
class BenchmarkExecution {
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var load: LoadDefinition
    lateinit var resources: ResourceDefinition
    lateinit var slos: List<Slo>
    lateinit var execution: Execution
    lateinit var configOverrides: List<ConfigurationOverride>

    @RegisterForReflection
    class Execution {
        lateinit var strategy: String
        var duration by Delegates.notNull<Long>()
        var repetitions by Delegates.notNull<Int>()
        lateinit var restrictions: List<String>
    }

    @RegisterForReflection
    class Slo {
        lateinit var sloType: String
        var threshold by Delegates.notNull<Int>()
        lateinit var prometheusUrl: String
        lateinit var externalSloUrl: String
        var offset by Delegates.notNull<Int>()
        var warmup by Delegates.notNull<Int>()
    }

    @RegisterForReflection
    class LoadDefinition {
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    @RegisterForReflection
    class ResourceDefinition {
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
