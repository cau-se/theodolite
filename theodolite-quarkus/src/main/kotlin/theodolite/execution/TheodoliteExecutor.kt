package theodolite.execution

import com.google.gson.GsonBuilder
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.patcher.PatcherDefinitionFactory
import theodolite.strategies.StrategyFactory
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.Config
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.io.File
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Path
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
     * @return a [Config], that contains a list of [LoadDimension]s,
     *          a list of [Resource]s , and the [CompositeStrategy].
     * The [CompositeStrategy] is configured and able to find the minimum required resource for the given load.
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
                slo = config.slos[0],
                executionId = config.executionId,
                loadGenerationDelay = config.execution.loadGenerationDelay,
                afterTeardownDelay = config.execution.afterTeardownDelay
            )

        return Config(
            loads = config.load.loadValues.map { load -> LoadDimension(load, loadDimensionPatcherDefinition) },
            resources = config.resources.resourceValues.map { resource ->
                Resource(
                    resource,
                    resourcePatcherDefinition
                )
            },
            compositeStrategy = CompositeStrategy(
                benchmarkExecutor = executor,
                searchStrategy = strategyFactory.createSearchStrategy(executor, config.execution.strategy),
                restrictionStrategies = strategyFactory.createRestrictionStrategy(
                    results,
                    config.execution.restrictions
                )
            )
        )
    }

    fun getExecution(): BenchmarkExecution {
        return this.config
    }

    fun getBenchmark(): KubernetesBenchmark {
        return this.kubernetesBenchmark
    }

    private fun getResultFolderString(): String {
        var resultsFolder: String = System.getenv("RESULTS_FOLDER") ?: ""
        val createResultsFolder = System.getenv("CREATE_RESULTS_FOLDER") ?: "false"

        if (resultsFolder != ""){
            logger.info { "RESULT_FOLDER: $resultsFolder" }
            val directory = File(resultsFolder)
            if (!directory.exists()) {
                logger.error { "Folder $resultsFolder does not exist" }
                if (createResultsFolder.toBoolean()) {
                    directory.mkdirs()
                } else {
                    throw IllegalArgumentException("Result folder not found")
                }
            }
            resultsFolder += "/"
        }
        return  resultsFolder
    }

    /**
     * Run all experiments which are specified in the corresponding
     * execution and benchmark objects.
     */
    fun run() {
        val resultsFolder = getResultFolderString()
        storeAsFile(this.config, "$resultsFolder${this.config.executionId}-execution-configuration")
        storeAsFile(kubernetesBenchmark, "$resultsFolder${this.config.executionId}-benchmark-configuration")

        val config = buildConfig()
        // execute benchmarks for each load
        for (load in config.loads) {
            if (executor.run.get()) {
                config.compositeStrategy.findSuitableResource(load, config.resources)
            }
        }
        storeAsFile(config.compositeStrategy.benchmarkExecutor.results, "$resultsFolder${this.config.executionId}-result")
    }

    private fun <T> storeAsFile(saveObject: T, filename: String) {
        val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()

        PrintWriter(filename).use { pw ->
            pw.println(gson.toJson(saveObject))
        }
    }
}
