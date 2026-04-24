package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

class SloFactory {

    fun createSlos(
        execution: BenchmarkExecution,
        benchmark: KubernetesBenchmark,
        resolvedSliNames: Set<String>
    ): List<Slo> {
        val resultSlos = benchmark.slos.map { benchmarkSlo ->
            val merged = Slo().also {
                it.name = benchmarkSlo.name
                it.sli = benchmarkSlo.sli
                it.warmupSeconds = benchmarkSlo.warmupSeconds
                it.queryAggregation = benchmarkSlo.queryAggregation
                it.repetitionAggregation = benchmarkSlo.repetitionAggregation
                it.operator = benchmarkSlo.operator
                it.threshold = benchmarkSlo.threshold
                it.thresholdRelToLoad = benchmarkSlo.thresholdRelToLoad
                it.thresholdRelToResources = benchmarkSlo.thresholdRelToResources
                it.thresholdFromExpression = benchmarkSlo.thresholdFromExpression
                it.externalSloChecker = benchmarkSlo.externalSloChecker
            }
            val override = execution.slos?.find { it.name == benchmarkSlo.name }
            if (override != null) {
                override.sli?.let { merged.sli = it }
                override.warmupSeconds?.let { merged.warmupSeconds = it }
                override.queryAggregation?.let { merged.queryAggregation = it }
                override.repetitionAggregation?.let { merged.repetitionAggregation = it }
                override.operator?.let { merged.operator = it }
                override.threshold?.let { merged.threshold = it }
                override.thresholdRelToLoad?.let { merged.thresholdRelToLoad = it }
                override.thresholdRelToResources?.let { merged.thresholdRelToResources = it }
                override.thresholdFromExpression?.let { merged.thresholdFromExpression = it }
                override.externalSloChecker?.let { merged.externalSloChecker = it }
            }
            merged
        }

        val unknownSlis = resultSlos.map { it.sli }.filter { it !in resolvedSliNames }
        if (unknownSlis.isNotEmpty()) {
            throw IllegalArgumentException(
                "SLOs reference unknown SLI(s): ${unknownSlis.joinToString()}. " +
                    "Known SLIs: ${resolvedSliNames.joinToString()}"
            )
        }

        return resultSlos
    }
}
