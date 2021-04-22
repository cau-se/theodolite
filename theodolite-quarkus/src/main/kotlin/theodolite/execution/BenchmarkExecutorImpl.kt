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
    configurationOverrides: List<ConfigurationOverride?>,
    slo: BenchmarkExecution.Slo,
    executionId: Int,
    loadGenerationDelay: Long
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo, executionId, loadGenerationDelay) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val benchmarkDeployment = benchmark.buildDeployment(load, res, configurationOverrides, loadGenerationDelay)

        try {
            benchmarkDeployment.setup()
            this.waitAndLog()
        } catch (e: Exception) {
            logger.error { "Error while setting up experiment with id ${this.executionId}." }
            logger.error { "Error is: $e" }
            this.run.set(false)
        }

        /**
         * Analyse the experiment, if [run] is true, otherwise the experiment was canceled by the user.
         */
        if (this.run.get()) {
            result = AnalysisExecutor(slo = slo, executionId = executionId).analyze(
                load = load,
                res = res,
                executionDuration = executionDuration
            )
            this.results.setResult(Pair(load, res), result)
        }

        try {
            benchmarkDeployment.teardown()
        } catch (e: Exception) {
            logger.warn { "Error while tearing down the benchmark deployment." }
            logger.debug { "Teardown failed, caused by: $e" }
        }

        return result
    }
}
