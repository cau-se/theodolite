package theodolite.execution

import theodolite.benchmark.Benchmark
import theodolite.evaluation.SLOChecker
import theodolite.evaluation.SLOCheckerImpl
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.sql.Time
import java.time.Duration
import java.time.Instant

class BenchmarkExecutorImpl(
    benchmark: Benchmark,
    results: Results,
    executionDuration: Duration,
    private val configurationOverrides: List<ConfigurationOverride>
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides)
        benchmarkDeployment.setup()
        this.waitAndLog()
        benchmarkDeployment.teardown()
        // todo evaluate
        val result = SLOCheckerImpl("http://localhost:32656")
            .evaluate(Instant.now().toEpochMilli() - executionDuration.toMillis(),
                Instant.now().toEpochMilli())
        this.results.setResult(Pair(load, res), result)
        return result
    }
}
