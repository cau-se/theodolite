package theodolite.benchmark

import theodolite.util.OverridePatcherDefinition
import theodolite.util.PatcherDefinition
import kotlin.properties.Delegates


class BenchmarkContext() {
    lateinit var name: String
    lateinit var benchmark: String
    lateinit var loads: List<Int>
    lateinit var resources: List<Int>
    lateinit var slos: List<Slo>
    lateinit var execution: Execution
    lateinit var configOverrides: List<OverridePatcherDefinition>

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
}
