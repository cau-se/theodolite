package theodolite.execution

import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.evaluation.AnalysisExecutor
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

private val logger = KotlinLogging.logger {}

@RegisterForReflection
class BenchmarkExecutorImpl(
    benchmark: Benchmark,
    results: Results,
    executionDuration: Duration,
    private val configurationOverrides: List<ConfigurationOverride?>,
    slo: BenchmarkExecution.Slo,
    executionId: Int
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo, executionId) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides)

        try {
            benchmarkDeployment.setup()
            this.waitAndLog()
        } catch(e: Exception) {
            logger.error { "Error while setup experiment." }
            logger.error { "Error is: $e" }
            this.run.set(false)
        }

        if (this.run.get()) {
            result =
                AnalysisExecutor(slo = slo, executionId = executionId).analyze(load = load, res = res, executionDuration = executionDuration)
            this.results.setResult(Pair(load, res), result)
        }
        benchmarkDeployment.teardown()
        return result
    }
}
