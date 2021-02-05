package theodolite.execution

import mu.KotlinLogging
import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class BenchmarkExecutorImpl(benchmark: Benchmark, results: Results, executionDuration: Duration) : BenchmarkExecutor(benchmark, results, executionDuration) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        benchmark.start(load, res)
        this.waitAndLog()
        benchmark.clearClusterEnvironment()
        // todo evaluate
        val result = false // if success else false
        this.results.setResult(Pair(load, res), result)
        return result;
    }
}