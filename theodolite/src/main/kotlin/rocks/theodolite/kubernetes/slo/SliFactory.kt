package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli

class SliFactory {

    fun createSlis(execution: BenchmarkExecution, benchmark: KubernetesBenchmark): List<Sli> {
        val resultSlis = benchmark.slis.map { benchmarkSli ->
            val merged = Sli().also {
                it.name = benchmarkSli.name
                it.provider = benchmarkSli.provider
                it.query = benchmarkSli.query
                it.intervalSeconds = benchmarkSli.intervalSeconds
                it.providerConfig = benchmarkSli.providerConfig.toMutableMap()
            }
            val override = execution.slis?.find { it.name == benchmarkSli.name }
            if (override != null) {
                override.provider?.let { merged.provider = it }
                override.query?.let { merged.query = it }
                override.intervalSeconds?.let { merged.intervalSeconds = it }
                override.providerConfig?.let { merged.providerConfig.putAll(it) }
            }
            merged
        }
        return resultSlis
    }
}
