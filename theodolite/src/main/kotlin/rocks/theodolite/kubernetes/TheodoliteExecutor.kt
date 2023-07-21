package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.core.*
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.StrategyFactory
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.patcher.PatcherDefinitionFactory
import rocks.theodolite.kubernetes.slo.SloFactory
import java.io.File
import java.time.Duration


private val logger = KotlinLogging.logger {}

/**
 * The Theodolite executor runs all the experiments defined with the given execution and benchmark configuration.
 *
 * @property benchmarkExecution Configuration of a execution
 * @property benchmark Configuration of a benchmark
 * @constructor Create empty Theodolite executor
 */
class TheodoliteExecutor(
    private val benchmarkExecution: BenchmarkExecution,
    private val benchmark: KubernetesBenchmark,
    private val client: NamespacedKubernetesClient
) {
    /**
     * An executor object, configured with the specified benchmark, evaluation method, experiment duration
     * and overrides which are given in the execution.
     */
    lateinit var experimentRunner: ExperimentRunner

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
                this.benchmark.resourceTypes
            )

        val loadDimensionPatcherDefinition =
            PatcherDefinitionFactory().createPatcherDefinition(
                benchmarkExecution.load.loadType,
                this.benchmark.loadTypes
            )

        val slos = SloFactory().createSlos(this.benchmarkExecution, this.benchmark)

        experimentRunner =
            ExperimentRunnerImpl(
                benchmarkDeploymentBuilder = KubernetesBenchmarkDeploymentBuilder(this.benchmark, this.client),
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
                resourcePatcherDefinitions = resourcePatcherDefinition,
                waitForResourcesEnabled = this.benchmark.waitForResourcesEnabled
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
            loads = benchmarkExecution.load.loadValues,
            resources = benchmarkExecution.resources.resourceValues,
            searchStrategy = strategyFactory.createSearchStrategy(
                experimentRunner,
                benchmarkExecution.execution.strategy.name,
                benchmarkExecution.execution.strategy.searchStrategy,
                benchmarkExecution.execution.strategy.restrictions,
                benchmarkExecution.execution.strategy.guessStrategy,
                results
            ),
            metric = Metric.from(benchmarkExecution.execution.metric)
        )
    }

    /**
     * Sets up the Infrastructure, increments the executionId, calls the [ExecutionRunner] that runs
     * all experiments which are specified in the corresponding execution and benchmark objects.
     */
    fun setupAndRunExecution() {
        setupInfrastructure()

        val ioHandler = IOHandler()
        val resultsFolder = ioHandler.getResultFolderURL()
        this.benchmarkExecution.executionId = getAndIncrementExecutionID(resultsFolder + "expID.txt")
        ioHandler.writeToJSONFile(
            this.benchmarkExecution,
            "${resultsFolder}exp${this.benchmarkExecution.executionId}-execution-configuration"
        )
        ioHandler.writeToJSONFile(
            benchmark,
            "${resultsFolder}exp${this.benchmarkExecution.executionId}-benchmark-configuration"
        )

        val config = buildConfig()

        val executionRunner = ExecutionRunner(
            config.searchStrategy, config.resources, config.loads, config.metric,
            this.benchmarkExecution.executionId
        )

        executionRunner.run()

        teardownInfrastructure()
    }

    private fun setupInfrastructure() {
        benchmark.infrastructure.beforeActions.forEach { it.exec(client = client) }
        val kubernetesManager = K8sManager(this.client)
        loadKubernetesResources(benchmark.infrastructure.resources, this.client)
            .map { it.second }
            .forEach { kubernetesManager.deploy(it) }
    }

    private fun teardownInfrastructure() {
        val kubernetesManager = K8sManager(this.client)
        loadKubernetesResources(benchmark.infrastructure.resources, this.client)
            .map { it.second }
            .forEach { kubernetesManager.remove(it) }
        benchmark.infrastructure.afterActions.forEach { it.exec(client = client) }
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

    private fun calculateMetric(xValues: List<Int>, results: Results): List<List<String>> {
        return xValues.map { listOf(it.toString(), results.getOptimalYValue(it).toString()) }
    }

    fun getExecution(): BenchmarkExecution {
        return this.benchmarkExecution
    }
}
