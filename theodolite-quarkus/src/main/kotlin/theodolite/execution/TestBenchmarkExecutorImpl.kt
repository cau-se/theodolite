package theodolite.execution

import theodolite.benchmark.Benchmark
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.AbstractBenchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class TestBenchmarkExecutorImpl(private val mockResults: Array<Array<Boolean>>, benchmark: Benchmark, results: Results):
    BenchmarkExecutor(benchmark, results, executionDuration = Duration.ofSeconds(1),
        override = emptyMap<String,String>()
    ) {

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val result = this.mockResults[load.get()][res.get()]

        this.results.setResult(Pair(load, res), result)
        return result;
    }
}