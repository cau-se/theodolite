package theodolite.execution

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteYamlExecutor")
object TheodoliteYamlExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Theodolite started" }

        val executionPath = System.getenv("THEODOLITE_EXECUTION") ?: "./config/BenchmarkExecution.yaml"
        val benchmarkPath = System.getenv("THEODOLITE_BENCHMARK_TYPE") ?: "./config/BenchmarkType.yaml"
        val appResource = System.getenv("THEODOLITE_RESOURCES") ?: "./config"

        logger.info { "Using $executionPath for BenchmarkExecution" }
        logger.info { "Using $benchmarkPath for BenchmarkType" }
        logger.info { "Using $appResource for Resources" }


        // load the BenchmarkExecution and the BenchmarkType
        val parser = YamlParser()
        val benchmarkExecution =
            parser.parse(path = executionPath, E = BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse(path = benchmarkPath, E = KubernetesBenchmark::class.java)!!
        benchmark.path = appResource

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
        logger.info { "Theodolite finished" }
        exitProcess(0)
    }
}
