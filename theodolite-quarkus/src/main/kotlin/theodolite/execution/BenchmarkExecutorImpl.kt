package theodolite.execution

import theodolite.benchmark.Benchmark
import theodolite.util.*
import java.time.Duration

class BenchmarkExecutorImpl(benchmark: Benchmark, results: Results, executionDuration: Duration, private val overrides: List<OverridePatcherDefinition>) : BenchmarkExecutor(benchmark, results, executionDuration, overrides) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.overrides)
        benchmarkDeployment.setup()
        this.waitAndLog()
        benchmarkDeployment.teardown()
        // todo evaluate
        val result = false // if success else false
        this.results.setResult(Pair(load, res), result)
        return result;
    }
}