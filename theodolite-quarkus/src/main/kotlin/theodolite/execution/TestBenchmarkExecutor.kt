package theodolite.execution

import theodolite.execution.BenchmarkExecutor
import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

class TestBenchmarkExecutor(private val mockResults: Array<Array<Boolean>>, benchmark: Benchmark, results: Results):
    BenchmarkExecutor(benchmark, results) {

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val result = this.mockResults[load.get()][res.get()]

        System.out.println("load :" + load.get().toString() + ", res: " + res.get().toString() + ", res: " + result)

        this.results.setResult(Pair(load, res), result)
        return result;
    }
}