package theodolite.execution

import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class TheodoliteYamlExecutor {
    private val parser = YamlParser()

    fun start() {
        logger.info { "Theodolite started" }

        val executionPath = System.getenv("THEODOLITE_EXECUTION") ?: "./config/example-execution-yaml-resource.yaml"
        val benchmarkPath = System.getenv("THEODOLITE_BENCHMARK") ?: "./config/example-benchmark-yaml-resource.yaml"

        logger.info { "Using $executionPath for BenchmarkExecution" }
        logger.info { "Using $benchmarkPath for BenchmarkType" }


        // load the BenchmarkExecution and the BenchmarkType
        val benchmarkExecution =
            parser.parse(path = executionPath, E = BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse(path = benchmarkPath, E = KubernetesBenchmark::class.java)!!

        // Add shutdown hook
        // Use thread{} with start = false, else the thread will start right away
        val shutdown = thread(start = false) { Shutdown(benchmarkExecution, benchmark).run() }
        Runtime.getRuntime().addShutdownHook(shutdown)

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
        logger.info { "Theodolite finished" }
        Runtime.getRuntime().removeShutdownHook(shutdown)
        exitProcess(0)
    }
}
