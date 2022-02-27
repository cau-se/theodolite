package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark

class SloFactory {

    fun createSlos(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<KubernetesBenchmark.Slo> {
        var benchmarkSlos = benchmark.slos
        var executionSlos = execution.slos
        //TODO: test if we can actually overwrite entries of the objects
        for(executionSlo in executionSlos) {
            for(benchmarkSlo in benchmarkSlos) {
                if(executionSlo.name == benchmarkSlo.name) {
                    benchmarkSlo.offset = executionSlo.offset ?: benchmarkSlo.offset
                    benchmarkSlo.prometheusUrl = executionSlo.prometheusUrl ?: benchmarkSlo.prometheusUrl
                    for(executionProperty in executionSlo.properties) {
                        benchmarkSlo.properties[executionProperty.key] = executionProperty.value
                    }
                }
            }
        }
        return benchmarkSlos
    }
}