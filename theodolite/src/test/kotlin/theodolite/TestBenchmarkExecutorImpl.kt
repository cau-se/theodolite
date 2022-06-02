package theodolite

import theodolite.benchmark.Benchmark
import theodolite.benchmark.Slo
import theodolite.execution.BenchmarkExecutor
import theodolite.util.Results
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
