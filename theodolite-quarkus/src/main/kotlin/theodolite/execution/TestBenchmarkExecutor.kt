package theodolite.execution

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TestBenchmarkExecutor(private val mockResults: Array<Array<Boolean>>, benchmark: Benchmark, results: Results):
    BenchmarkExecutor(benchmark, results, executionDuration = Duration.ofSeconds(1)) {

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val result = this.mockResults[load.get()][res.get()]

        this.results.setResult(Pair(load, res), result)
        return result;
    }
}