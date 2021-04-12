package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}


/**
 * The Theodolite yaml executor loads the required configurations
 * of the executions and the benchmark from yaml files and run the
 * corresponding experiments.
 *
 * The location of the execution, benchmarks and Kubernetes resource
 * files can be configured via the following environment variables:
 * `THEODOLITE_EXECUTION`
 *
 * `THEODOLITE_BENCHMARK`
 *
 * `THEODOLITE_APP_RESOURCES`
 *
 * @constructor Create empty Theodolite yaml executor
 */
class TheodoliteYamlExecutor {
    private val parser = YamlParser()

    fun start() {
        logger.info { "Theodolite started" }

        val executionPath = System.getenv("THEODOLITE_EXECUTION") ?: "./config/BenchmarkExecution.yaml"
        val benchmarkPath = System.getenv("THEODOLITE_BENCHMARK") ?: "./config/BenchmarkType.yaml"
        val appResource = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"

        logger.info { "Using $executionPath for BenchmarkExecution" }
        logger.info { "Using $benchmarkPath for BenchmarkType" }
        logger.info { "Using $appResource for Resources" }


        // load the BenchmarkExecution and the BenchmarkType
        val benchmarkExecution =
            parser.parse(path = executionPath, E = BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse(path = benchmarkPath, E = KubernetesBenchmark::class.java)!!
        benchmark.path = appResource

        val shutdown = Shutdown(benchmarkExecution, benchmark)
        Runtime.getRuntime().addShutdownHook(thread { shutdown.run()})

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
        logger.info { "Theodolite finished" }
        Runtime.getRuntime().removeShutdownHook(thread { shutdown.run()})
        exitProcess(0)
    }
}
