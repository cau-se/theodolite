package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark

class SloFactory {

    fun createSlos(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<KubernetesBenchmark.Slo> {
        var benchmarkSlos = benchmark.slos
        var executionSlos = execution.slos

        for(executionSlo in executionSlos) {
            for(i in 0..benchmarkSlos.size) {
                if(executionSlo.name == benchmarkSlos[i].name) {
                    benchmarkSlos[i].offset = executionSlo.offset ?: benchmarkSlos[i].offset
                    benchmarkSlos[i].prometheusUrl = executionSlo.prometheusUrl ?: benchmarkSlos[i].prometheusUrl
                    if (executionSlo.properties != null) {
                        for (executionProperty in executionSlo.properties!!) {
                            benchmarkSlos[i].properties[executionProperty.key] = executionProperty.value
                        }
                    }
                }
            }
        }
        return benchmarkSlos
    }
}