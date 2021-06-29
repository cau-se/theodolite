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
    configurationOverrides: List<ConfigurationOverride?>,
    slo: BenchmarkExecution.Slo,
    repetitions: Int,
    executionId: Int,
    loadGenerationDelay: Long,
    afterTeardownDelay: Long
) : BenchmarkExecutor(benchmark, results, executionDuration, configurationOverrides, slo, repetitions, executionId, loadGenerationDelay, afterTeardownDelay) {
    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val executionIntervals: MutableList<Pair<Instant, Instant>> = ArrayList()

        for (i in 1.rangeTo(repetitions)) {
            logger.info { "Run repetition $i/$repetitions" }
            if (this.run.get()) {
                executionIntervals.add(runSingleExperiment(load,res))
            } else {
                break
            }
        }

        /**
         * Analyse the experiment, if [run] is true, otherwise the experiment was canceled by the user.
         */
        if (this.run.get()) {
            result =AnalysisExecutor(slo = slo, executionId = executionId)
                    .analyze(
                        load = load,
                        res = res,
                        executionIntervals = executionIntervals)
            this.results.setResult(Pair(load, res), result)
        }
        return result
    }

    private fun runSingleExperiment(load: LoadDimension, res: Resource): Pair<Instant, Instant> {
        val benchmarkDeployment = benchmark.buildDeployment(load, res, this.configurationOverrides, this.loadGenerationDelay, this.afterTeardownDelay)
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
        try {
            benchmarkDeployment.teardown()
        } catch (e: Exception) {
            logger.warn { "Error while tearing down the benchmark deployment." }
            logger.debug { "Teardown failed, caused by: $e" }
        }
        return Pair(from,to)
    }
}
