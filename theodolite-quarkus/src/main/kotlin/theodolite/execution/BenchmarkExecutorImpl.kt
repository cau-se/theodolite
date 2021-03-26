package theodolite.execution

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.evaluation.AnalysisExecutor
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

@RegisterForReflection
class BenchmarkExecutorImpl(
    benchmark: Benchmark,
    results: Results,
    executionDuration: Duration,
    private val configurationOverrides: List<ConfigurationOverride?>,
    slo: BenchmarkExecution.Slo
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides)
        benchmarkDeployment.setup()
        this.waitAndLog()

        if (this.run) {
            result =
                AnalysisExecutor(slo = slo).analyse(load = load, res = res, executionDuration = executionDuration)
            this.results.setResult(Pair(load, res), result)
        }
        benchmarkDeployment.teardown()
        return result
    }
}
