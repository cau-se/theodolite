package theodolite.benchmark

import theodolite.util.ConfigurationOverride
import kotlin.properties.Delegates


class BenchmarkExecution() {
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var load: LoadDefinition
    lateinit var resources: ResourceDefinition
    lateinit var slos: List<Slo>
    lateinit var execution: Execution
    lateinit var configOverrides: List<ConfigurationOverride>

    class Execution() {
        lateinit var  strategy: String
        var duration by Delegates.notNull<Long>()
        var repititions by Delegates.notNull<Int>()
        lateinit var restrictions: List<String>
    }

    class Slo(){
        lateinit var sloType: String
        var threshold by Delegates.notNull<Int>()
    }

    class LoadDefinition() {
        lateinit var loadType: String
        lateinit var loadValues: List<Int>
    }

    class ResourceDefinition() {
        lateinit var resourceType: String
        lateinit var resourceValues: List<Int>
    }
}
