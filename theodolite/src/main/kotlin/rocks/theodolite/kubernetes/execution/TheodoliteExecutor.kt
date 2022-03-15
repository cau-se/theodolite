package rocks.theodolite.kubernetes.execution

import mu.KotlinLogging
import rocks.theodolite.core.ExecutionRunner
import rocks.theodolite.core.ExperimentRunner
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.patcher.PatcherDefinitionFactory
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.StrategyFactory
import rocks.theodolite.core.Config
import rocks.theodolite.core.IOHandler
import rocks.theodolite.core.Results
import rocks.theodolite.kubernetes.slo.SloFactory
import java.io.File
import java.time.Duration


private val logger = KotlinLogging.logger {}

/**
 * The Theodolite executor runs all the experiments defined with the given execution and benchmark configuration.
 *
 * @property benchmarkExecution Configuration of a execution
 * @property kubernetesBenchmark Configuration of a benchmark
 * @constructor Create empty Theodolite executor
 */
class TheodoliteExecutor(
        private val benchmarkExecution: BenchmarkExecution,
        private val kubernetesExecutionRunner: KubernetesExecutionRunner
) {
    /**
     * An executor object, configured with the specified benchmark, evaluation method, experiment duration
     * and overrides which are given in the execution.
     */
    lateinit var experimentRunner: ExperimentRunner
    private val kubernetesBenchmark = kubernetesExecutionRunner.kubernetesBenchmark

    /**
     * Creates all required components to start Theodolite.
     *
     * @return a [Config], that contains a list of LoadDimension s,
     *          a list of Resource s , and the [restrictionSearch].
     * The [SearchStrategy] is configured and able to find the minimum required resource for the given load.
     */
    private fun buildConfig(): Config {
        val results = Results(Metric.from(benchmarkExecution.execution.metric))
        val strategyFactory = StrategyFactory()

        val executionDuration = Duration.ofSeconds(benchmarkExecution.execution.duration)

        val resourcePatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                benchmarkExecution.resources.resourceType,
                this.kubernetesBenchmark.resourceTypes
            )

        val loadDimensionPatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                benchmarkExecution.loads.loadType,
                this.kubernetesBenchmark.loadTypes
            )

        val slos = SloFactory().createSlos(this.benchmarkExecution, this.kubernetesBenchmark)

        experimentRunner =
            ExperimentRunnerImpl(
                benchmark = kubernetesExecutionRunner,
                results = results,
                executionDuration = executionDuration,
                configurationOverrides = benchmarkExecution.configOverrides,
                slos = slos,
                repetitions = benchmarkExecution.execution.repetitions,
                executionId = benchmarkExecution.executionId,
                loadGenerationDelay = benchmarkExecution.execution.loadGenerationDelay,
                afterTeardownDelay = benchmarkExecution.execution.afterTeardownDelay,
                executionName = benchmarkExecution.name,
                loadPatcherDefinitions = loadDimensionPatcherDefinition,
                resourcePatcherDefinitions = resourcePatcherDefinition
            )

        if (benchmarkExecution.loads.loadValues != benchmarkExecution.loads.loadValues.sorted()) {
            benchmarkExecution.loads.loadValues = benchmarkExecution.loads.loadValues.sorted()
            logger.info {
                "Load values are not sorted correctly, Theodolite sorts them in ascending order." +
                        "New order is: ${benchmarkExecution.loads.loadValues}"
            }
        }

        if (benchmarkExecution.resources.resourceValues != benchmarkExecution.resources.resourceValues.sorted()) {
            benchmarkExecution.resources.resourceValues = benchmarkExecution.resources.resourceValues.sorted()
            logger.info {
                "Load values are not sorted correctly, Theodolite sorts them in ascending order." +
                        "New order is: ${benchmarkExecution.resources.resourceValues}"
            }
        }

        return Config(
            loads = benchmarkExecution.loads.loadValues,
            resources = benchmarkExecution.resources.resourceValues,
            searchStrategy = strategyFactory.createSearchStrategy(experimentRunner, benchmarkExecution.execution.strategy.name,
                    benchmarkExecution.execution.strategy.searchStrategy, benchmarkExecution.execution.strategy.restrictions,
                    benchmarkExecution.execution.strategy.guessStrategy, results),
            metric = Metric.from(benchmarkExecution.execution.metric)
        )
    }

    /**
     * Sets up the Infrastructure, increments the executionId, calls the [ExecutionRunner] that runs
     * all experiments which are specified in the corresponding execution and benchmark objects.
     */
    fun setupAndRunExecution() {
        kubernetesExecutionRunner.setupInfrastructure()

        val ioHandler = IOHandler()
        val resultsFolder = ioHandler.getResultFolderURL()
        this.benchmarkExecution.executionId = getAndIncrementExecutionID(resultsFolder + "expID.txt")
        ioHandler.writeToJSONFile(this.benchmarkExecution, "${resultsFolder}exp${this.benchmarkExecution.executionId}-execution-configuration")
        ioHandler.writeToJSONFile(
            kubernetesBenchmark,
            "${resultsFolder}exp${this.benchmarkExecution.executionId}-benchmark-configuration"
        )

        val config = buildConfig()

        val executionRunner = ExecutionRunner(config.searchStrategy, config.resources, config.loads,config.metric,
                                              this.benchmarkExecution.executionId)

        executionRunner.run()

        kubernetesExecutionRunner.teardownInfrastructure()
    }

    private fun getAndIncrementExecutionID(fileURL: String): Int {
        val ioHandler = IOHandler()
        var executionID = 0
        if (File(fileURL).exists()) {
            executionID = ioHandler.readFileAsString(fileURL).toInt() + 1
        }
        ioHandler.writeStringToTextFile(fileURL, (executionID).toString())
        return executionID
    }

    fun getExecution(): BenchmarkExecution {
        return this.benchmarkExecution
    }
}
