package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.patcher.PatcherDefinitionFactory
import theodolite.strategies.StrategyFactory
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.*
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
        private val kubernetesBenchmark: KubernetesBenchmark
) {
    /**
     * An executor object, configured with the specified benchmark, evaluation method, experiment duration
     * and overrides which are given in the execution.
     */
    lateinit var executor: BenchmarkExecutor

    /**
     * Creates all required components to start Theodolite.
     *
     * @return a [Config], that contains a list of [LoadDimension]s,
     *          a list of [Resource]s , and the [CompositeStrategy].
     * The [CompositeStrategy] is configured and able to find the minimum required resource for the given load.
     */
    private fun buildConfig(): Config {
        val results = Results()
        val strategyFactory = StrategyFactory()

        val executionDuration = Duration.ofSeconds(benchmarkExecution.execution.duration)

        val resourcePatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                benchmarkExecution.resources.resourceType,
                this.kubernetesBenchmark.resourceTypes
            )

        val loadDimensionPatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                benchmarkExecution.load.loadType,
                this.kubernetesBenchmark.loadTypes
            )

        val slos = SloFactory().createSlos(this.benchmarkExecution, this.kubernetesBenchmark)

        executor =
            BenchmarkExecutorImpl(
                benchmark = kubernetesBenchmark,
                results = results,
                executionDuration = executionDuration,
                configurationOverrides = benchmarkExecution.configOverrides,
                slos = slos,
                repetitions = benchmarkExecution.execution.repetitions,
                executionId = benchmarkExecution.executionId,
                loadGenerationDelay = benchmarkExecution.execution.loadGenerationDelay,
                afterTeardownDelay = benchmarkExecution.execution.afterTeardownDelay,
                executionName = benchmarkExecution.name
            )

        if (benchmarkExecution.load.loadValues != benchmarkExecution.load.loadValues.sorted()) {
            benchmarkExecution.load.loadValues = benchmarkExecution.load.loadValues.sorted()
            logger.info {
                "Load values are not sorted correctly, Theodolite sorts them in ascending order." +
                        "New order is: ${benchmarkExecution.load.loadValues}"
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
            loads = benchmarkExecution.load.loadValues.map { load -> LoadDimension(load, loadDimensionPatcherDefinition) },
            resources = benchmarkExecution.resources.resourceValues.map { resource ->
                Resource(
                    resource,
                    resourcePatcherDefinition
                )
            },
            compositeStrategy = CompositeStrategy(
                benchmarkExecutor = executor,
                searchStrategy = strategyFactory.createSearchStrategy(executor, benchmarkExecution.execution.strategy),
                restrictionStrategies = strategyFactory.createRestrictionStrategy(
                    results,
                    benchmarkExecution.execution.restrictions
                )
            )
        )
    }

    fun getExecution(): BenchmarkExecution {
        return this.benchmarkExecution
    }

    /**
     * Run all experiments which are specified in the corresponding
     * execution and benchmark objects.
     */
    fun run() {
        kubernetesBenchmark.setupInfrastructure()

        val ioHandler = IOHandler()
        val resultsFolder = ioHandler.getResultFolderURL()
        this.benchmarkExecution.executionId = getAndIncrementExecutionID(resultsFolder + "expID.txt")
        ioHandler.writeToJSONFile(this.benchmarkExecution, "${resultsFolder}exp${this.benchmarkExecution.executionId}-execution-configuration")
        ioHandler.writeToJSONFile(
            kubernetesBenchmark,
            "${resultsFolder}exp${this.benchmarkExecution.executionId}-benchmark-configuration"
        )

        val config = buildConfig()
        // execute benchmarks for each load
        try {
            for (load in config.loads) {
                if (executor.run.get()) {
                    config.compositeStrategy.findSuitableResource(load, config.resources)
                }
            }
        } finally {
            ioHandler.writeToJSONFile(
                config.compositeStrategy.benchmarkExecutor.results,
                "${resultsFolder}exp${this.benchmarkExecution.executionId}-result"
            )
            // Create expXYZ_demand.csv file
            ioHandler.writeToCSVFile(
                "${resultsFolder}exp${this.benchmarkExecution.executionId}_demand",
                calculateDemandMetric(config.loads, config.compositeStrategy.benchmarkExecutor.results),
                listOf("load","resources")
            )
        }
        kubernetesBenchmark.teardownInfrastructure()
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

    private fun calculateDemandMetric(loadDimensions: List<LoadDimension>, results: Results): List<List<String>> {
        return loadDimensions.map { listOf(it.get().toString(), results.getMinRequiredInstances(it).get().toString()) }
    }

}
