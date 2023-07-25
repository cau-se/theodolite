package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

class SloFactory {

    fun createSlos(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<Slo> {
        val resultSlos = benchmark.slos.toMutableList()

        val executionSlos = execution.slos
        if (executionSlos != null) {
            for (executionSlo in executionSlos) {
                for (resultSlo in resultSlos) {
                    if (executionSlo.name == resultSlo.name && executionSlo.properties != null) {
                        resultSlo.properties.putAll(executionSlo.properties!!)
                    }
                }
            }
        }

        return resultSlos
    }
}