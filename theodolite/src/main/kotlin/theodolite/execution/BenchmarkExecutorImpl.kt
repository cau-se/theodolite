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
    executionName: String,
    loadPatcherDefinitions: List<PatcherDefinition>,
    resourcePatcherDefinitions: List<PatcherDefinition>
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
    executionName,
    loadPatcherDefinitions,
    resourcePatcherDefinitions
) {
    private val eventCreator = EventCreator()
    private val mode = Configuration.EXECUTION_MODE

    override fun runExperiment(load: Int, resource: Int): Boolean {
        var result = false
        val executionIntervals: MutableList<Pair<Instant, Instant>> = ArrayList()

        for (i in 1.rangeTo(repetitions)) {
            if (this.run.get()) {
                logger.info { "Run repetition $i/$repetitions" }
                executionIntervals.add(runSingleExperiment(
                        load, resource))
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
                        resource = resource,
                        executionIntervals = executionIntervals,
                        metric = this.results.metric
                    )
            }

            result = (false !in experimentResults)
            this.results.setResult(Pair(load, resource), result)
        } else {
            throw ExecutionFailedException("The execution was interrupted")
        }
        return result
    }

    private fun runSingleExperiment(load: Int, resource: Int): Pair<Instant, Instant> {
        val benchmarkDeployment = benchmark.buildDeployment(
            load,
            this.loadPatcherDefinitions,
            resource,
            this.resourcePatcherDefinitions,
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
                    message = "load: $load, resources: $resource")
            }
        } catch (e: Exception) {
            this.run.set(false)

            if (mode == ExecutionModes.OPERATOR.value) {
                eventCreator.createEvent(
                    executionName = executionName,
                    type = "WARNING",
                    reason = "Start experiment failed",
                    message = "load: $load, resources: $resource")
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