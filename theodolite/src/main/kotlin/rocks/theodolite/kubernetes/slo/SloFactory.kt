package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

class SloFactory {

    fun createSlos(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<Slo> {
        val resultSlos = benchmark.slos.toMutableList()

        for (executionSlo in execution.slos) {
            for (resultSlo in resultSlos) {
                if (executionSlo.name == resultSlo.name && executionSlo.properties != null) {
                    resultSlo.properties.putAll(executionSlo.properties!!)
                }
            }
        }
        return resultSlos
    }
}