package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.patcher.PatcherDefinitionFactory
import theodolite.strategies.StrategyFactory
import theodolite.util.*
import java.io.File
import java.time.Duration


private val logger = KotlinLogging.logger {}

/**
 * The Theodolite executor runs all the experiments defined with the given execution and benchmark configuration.
 *
 * @property config Configuration of a execution
 * @property kubernetesBenchmark Configuration of a benchmark
 * @constructor Create empty Theodolite executor
 */
class TheodoliteExecutor(
    private val config: BenchmarkExecution,
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
     * @return a [Config], that contains a list of LoadDimension s,
     *          a list of Resource s , and the [restrictionSearch].
     * The [searchStrategy] is configured and able to find the minimum required resource for the given load.
     */
    private fun buildConfig(): Config {
        val results = Results()
        val strategyFactory = StrategyFactory()

        val executionDuration = Duration.ofSeconds(config.execution.duration)

        val resourcePatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                config.resources.resourceType,
                this.kubernetesBenchmark.resourceTypes
            )

        val loadDimensionPatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                config.load.loadType,
                this.kubernetesBenchmark.loadTypes
            )

        executor =
            BenchmarkExecutorImpl(
                benchmark = kubernetesBenchmark,
                results = results,
                executionDuration = executionDuration,
                configurationOverrides = config.configOverrides,
                slos = config.slos,
                repetitions = config.execution.repetitions,
                executionId = config.executionId,
                loadGenerationDelay = config.execution.loadGenerationDelay,
                afterTeardownDelay = config.execution.afterTeardownDelay,
                executionName = config.name,
                loadPatcherDefinitions = loadDimensionPatcherDefinition,
                resourcePatcherDefinitions = resourcePatcherDefinition
            )

        if (config.load.loadValues != config.load.loadValues.sorted()) {
            config.load.loadValues = config.load.loadValues.sorted()
            logger.info {
                "Load values are not sorted correctly, Theodolite sorts them in ascending order." +
                        "New order is: ${config.load.loadValues}"
            }
        }

        if (config.resources.resourceValues != config.resources.resourceValues.sorted()) {
            config.resources.resourceValues = config.resources.resourceValues.sorted()
            logger.info {
                "Load values are not sorted correctly, Theodolite sorts them in ascending order." +
                        "New order is: ${config.resources.resourceValues}"
            }
        }

        return Config(
            loads = config.load.loadValues,
            loadPatcherDefinitions = loadDimensionPatcherDefinition,
            resources = config.resources.resourceValues,
            resourcePatcherDefinitions = resourcePatcherDefinition,
            searchStrategy = strategyFactory.createSearchStrategy(executor, config.execution.strategy, results),
            metric = config.execution.metric
        )
    }

    fun getExecution(): BenchmarkExecution {
        return this.config
    }

    /**
     * Run all experiments which are specified in the corresponding
     * execution and benchmark objects.
     */
    fun run() {
        kubernetesBenchmark.setupInfrastructure()

        val ioHandler = IOHandler()
        val resultsFolder = ioHandler.getResultFolderURL()
        this.config.executionId = getAndIncrementExecutionID(resultsFolder + "expID.txt")
        ioHandler.writeToJSONFile(this.config, "${resultsFolder}exp${this.config.executionId}-execution-configuration")
        ioHandler.writeToJSONFile(
            kubernetesBenchmark,
            "${resultsFolder}exp${this.config.executionId}-benchmark-configuration"
        )

        val config = buildConfig()

        //execute benchmarks for each load for the demand metric, or for each resource amount for capacity metric
        try {
            if (config.metric == "demand") {
                //demand metric
                for (load in config.loads) {
                    if (executor.run.get()) {
                        config.searchStrategy.findSuitableResource(load, config.resources)
                    }
                }
            } else {
                //capacity metric
                for (resource in config.resources) {
                    if (executor.run.get()) {
                        config.searchStrategy.findSuitableLoad(resource, config.loads)
                    }
                }
            }

        } finally {
            ioHandler.writeToJSONFile(
                config.searchStrategy.benchmarkExecutor.results,
                "${resultsFolder}exp${this.config.executionId}-result"
            )
            // Create expXYZ_demand.csv file
            ioHandler.writeToCSVFile(
                "${resultsFolder}exp${this.config.executionId}_demand",
                calculateDemandMetric(config.loads, config.searchStrategy.benchmarkExecutor.results),
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

    private fun calculateDemandMetric(loads: List<Int>, results: Results): List<List<String>> {
        return loads.map { listOf(it.toString(), results.getMinRequiredInstances(it).toString()) }
    }

}
