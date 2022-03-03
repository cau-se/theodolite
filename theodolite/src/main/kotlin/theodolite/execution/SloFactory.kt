package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.benchmark.Slo

class SloFactory {

    fun createSlos(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<Slo> {
        var benchmarkSlos = benchmark.slos
        var executionSlos = execution.slos

        for(executionSlo in executionSlos) {
            for(i in 0 until benchmarkSlos.size) {
                if(executionSlo.name == benchmarkSlos[i].name && executionSlo.properties != null) {
                    for (executionProperty in executionSlo.properties!!) {
                        benchmarkSlos[i].properties[executionProperty.key] = executionProperty.value
                    }
                }
            }
        }
        return benchmarkSlos
    }
}