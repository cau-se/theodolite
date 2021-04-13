package theodolite.execution

import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkExecution
import theodolite.evaluation.AnalysisExecutor
import theodolite.util.*
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

@RegisterForReflection
class BenchmarkExecutorImpl(
    benchmark: Benchmark,
    results: Results,
    executionDuration: Duration,
    private val configurationOverrides: List<ConfigurationOverride?>,
    slo: BenchmarkExecution.Slo,
    repetitions: Int
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo, repetitions) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val executionIntervals: MutableList<Pair<Instant, Instant>> = ArrayList(repetitions)
        executionIntervals
        for (i in 1.rangeTo(repetitions)) {
            if (this.run.get()) {
                executionIntervals.add(runSingleExperiment(load,res))
            }
        }

        if (this.run.get()) {
            result =
                AnalysisExecutor(slo = slo).analyze(load = load, res = res, executionIntervals = executionIntervals)
            this.results.setResult(Pair(load, res), result)
        }
        return result
    }

    private fun runSingleExperiment(load: LoadDimension, res: Resource): Pair<Instant, Instant> {
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides)
        val from = Instant.now()
        try {
            benchmarkDeployment.setup()
            this.waitAndLog()
        } catch (e: Exception) {
            logger.error { "Error while setup experiment." }
            logger.error { "Error is: $e" }
            this.run.set(false)
        }
        val to = Instant.now()
        benchmarkDeployment.teardown()
        return Pair(from,to)
    }
}
