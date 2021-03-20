package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.evaluation.AnalysisExecutor
import theodolite.evaluation.SloCheckerFactory
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

class BenchmarkExecutorImpl(
    benchmark: Benchmark,
    results: Results,
    executionDuration: Duration,
    private val configurationOverrides: List<ConfigurationOverride>,
    slo: BenchmarkExecution.Slo
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides)
        benchmarkDeployment.setup()
        this.waitAndLog()

        var result = AnalysisExecutor().analyse(load,executionDuration,res,slo)

        benchmarkDeployment.teardown()

        this.results.setResult(Pair(load, res), result)
        return result
    }
}
