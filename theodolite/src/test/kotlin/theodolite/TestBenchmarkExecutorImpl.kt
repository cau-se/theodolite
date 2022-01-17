package theodolite

import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resources
import theodolite.util.Results
import java.time.Duration

class TestBenchmarkExecutorImpl(
    private val mockResults: Array<Array<Boolean>>,
    benchmark: Benchmark,
    results: Results,
    slo: List<BenchmarkExecution.Slo>,
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
        executionName = "test-execution"
    ) {

    override fun runExperiment(load: LoadDimension, res: Resources): Boolean {
        val result = this.mockResults[load.get()][res.get()]
        this.results.setResult(Pair(load, res), result)
        return result
    }
}
