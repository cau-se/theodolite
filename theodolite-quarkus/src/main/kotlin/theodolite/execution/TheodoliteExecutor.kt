package theodolite.execution

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
import java.time.Duration
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}


class TheodoliteExecutor(
    private var config: BenchmarkExecution,
    private var kubernetesBenchmark: KubernetesBenchmark
) {
    private val executionThread = Thread {
        if(config == null || kubernetesBenchmark == null) {
            logger.error { "Execution or Benchmark not found" }
        }else {
            val config = buildConfig()
            // execute benchmarks for each load
            for (load in config.loads) {
                config.compositeStrategy.findSuitableResource(load, config.resources)
            }
        }
    }

    var isRunning = false

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

        val executor =
            BenchmarkExecutorImpl(
                kubernetesBenchmark,
                results,
                executionDuration,
                config.configOverrides,
                config.slos[0]
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

    fun run() {
        isRunning = true
        executionThread.run()
    }

    fun stop() {
        isRunning = false
        executionThread.interrupt()
    }

    fun setExecution(config: BenchmarkExecution) {
        this.config = config
    }
    fun setBenchmark(benchmark: KubernetesBenchmark) {
        this.kubernetesBenchmark = benchmark
    }

    constructor() {}

    }
