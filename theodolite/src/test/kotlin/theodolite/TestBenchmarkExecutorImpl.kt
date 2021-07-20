package theodolite

import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TestBenchmarkExecutorImpl(
    private val mockResults: Array<Array<Boolean>>,
    benchmark: Benchmark,
    results: Results,
    slo: BenchmarkExecution.Slo,
    executionId: Int,
    loadGenerationDelay: Long,
    afterTeardownDelay: Long
) :
    BenchmarkExecutor(
        benchmark,
        results,
        executionDuration = Duration.ofSeconds(1),
        configurationOverrides = emptyList(),
        slo = slo,
        repetitions = 1,
        executionId = executionId,
        loadGenerationDelay = loadGenerationDelay,
        afterTeardownDelay = afterTeardownDelay
    ) {

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val result = this.mockResults[load.get()][res.get()]
        this.results.setResult(Pair(load, res), result)
        return result
    }
}
