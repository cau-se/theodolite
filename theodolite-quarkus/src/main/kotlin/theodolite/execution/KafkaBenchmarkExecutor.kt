package theodolite.execution

import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class KafkaBenchmarkExecutor(benchmark: Benchmark, results: Results, executionDuration: Duration) : BenchmarkExecutor(benchmark, results, executionDuration) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        benchmark.start()
        // wait
        benchmark.stop()
        // evaluate
        val result = false // if success else false
        this.results.setResult(Pair(load, res), result)
        return result;
    }
}