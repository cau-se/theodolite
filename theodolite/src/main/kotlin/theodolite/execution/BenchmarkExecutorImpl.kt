package theodolite.execution

import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.benchmark.Benchmark
import theodolite.benchmark.Slo
import theodolite.evaluation.AnalysisExecutor
import theodolite.execution.operator.EventCreator
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
        slos: List<Slo>,
        repetitions: Int,
        executionId: Int,
        loadGenerationDelay: Long,
        afterTeardownDelay: Long,
        executionName: String
) : BenchmarkExecutor(
    benchmark,
    results,
    executionDuration,
    configurationOverrides,
    slos,
    repetitions,
    executionId,
    loadGenerationDelay,
    afterTeardownDelay,
    executionName
) {
    private val eventCreator = EventCreator()
    private val mode = Configuration.EXECUTION_MODE

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        var result = false
        val executionIntervals: MutableList<Pair<Instant, Instant>> = ArrayList()

        for (i in 1.rangeTo(repetitions)) {
            if (this.run.get()) {
                logger.info { "Run repetition $i/$repetitions" }
                executionIntervals.add(runSingleExperiment(load, res))
            } else {
                break
            }
        }

        /**
         * Analyse the experiment, if [run] is true, otherwise the experiment was canceled by the user.
         */
        if (this.run.get()) {
            val experimentResults = slos.map {
                AnalysisExecutor(slo = it, executionId = executionId)
                    .analyze(
                        load = load,
                        res = res,
                        executionIntervals = executionIntervals
                    )
            }

            result = (false !in experimentResults)
            this.results.setResult(Pair(load, res), result)
        }

        if(!this.run.get()) {
            throw ExecutionFailedException("The execution was interrupted")
        }

        return result
    }

    private fun runSingleExperiment(load: LoadDimension, res: Resource): Pair<Instant, Instant> {
        val benchmarkDeployment = benchmark.buildDeployment(
            load,
            res,
            this.configurationOverrides,
            this.loadGenerationDelay,
            this.afterTeardownDelay
        )
        val from = Instant.now()

        try {
            benchmarkDeployment.setup()
            this.waitAndLog()
            if (mode == ExecutionModes.OPERATOR.value) {
                eventCreator.createEvent(
                    executionName = executionName,
                    type = "NORMAL",
                    reason = "Start experiment",
                    message = "load: ${load.get()}, resources: ${res.get()}")
            }
        } catch (e: Exception) {
            this.run.set(false)

            if (mode == ExecutionModes.OPERATOR.value) {
                eventCreator.createEvent(
                    executionName = executionName,
                    type = "WARNING",
                    reason = "Start experiment failed",
                    message = "load: ${load.get()}, resources: ${res.get()}")
            }
            throw ExecutionFailedException("Error during setup the experiment", e)
        }
        val to = Instant.now()
        try {
            benchmarkDeployment.teardown()
            if (mode == ExecutionModes.OPERATOR.value) {
                eventCreator.createEvent(
                    executionName = executionName,
                    type = "NORMAL",
                    reason = "Stop experiment",
                    message = "Teardown complete")
            }
        } catch (e: Exception) {
            if (mode == ExecutionModes.OPERATOR.value) {
                eventCreator.createEvent(
                    executionName = executionName,
                    type = "WARNING",
                    reason = "Stop experiment failed",
                    message = "Teardown failed: ${e.message}")
            }
            throw ExecutionFailedException("Error during teardown the experiment", e)
        }
        return Pair(from, to)
    }
}