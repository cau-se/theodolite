package theodolite

import rocks.theodolite.core.util.Results
import rocks.theodolite.kubernetes.benchmark.Benchmark
import rocks.theodolite.kubernetes.benchmark.Slo
import rocks.theodolite.kubernetes.execution.BenchmarkExecutor
import java.time.Duration

class TestBenchmarkExecutorImpl(
        private val mockResults: Array<Array<Boolean>>,
        benchmark: Benchmark,
        results: Results,
        slo: List<Slo>,
        executionId: Int,
        loadGenerationDelay: Long,
        afterTeardownDelay: Long
) :
    BenchmarkExecutor(
        benchmark,
        results,
        executionDuration = Duration.ofSeconds(1),
        configurationOverrides = emptyList(),
        slos = slo,
        repetitions = 1,
        executionId = executionId,
        loadGenerationDelay = loadGenerationDelay,
        afterTeardownDelay = afterTeardownDelay,
        executionName = "test-execution",
        loadPatcherDefinitions = emptyList(),
        resourcePatcherDefinitions = emptyList()
    ) {

    override fun runExperiment(load: Int, resource: Int): Boolean {
        val result = this.mockResults[load][resource]
        this.results.setResult(Pair(load, resource), result)
        return result
    }
}
