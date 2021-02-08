package theodolite.execution

import theodolite.util.AbstractBenchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

class BenchmarkExecutorImpl(benchmark: AbstractBenchmark, results: Results, executionDuration: Duration) : BenchmarkExecutor(benchmark, results, executionDuration) {
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