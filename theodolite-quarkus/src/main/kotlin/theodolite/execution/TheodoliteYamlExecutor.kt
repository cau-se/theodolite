package theodolite.execution

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteYamlExecutor")
object TheodoliteYamlExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Theodolite started" }

        // load the BenchmarkExecution and the BenchmarkType
        val parser = YamlParser()
        val benchmarkExecution =
            parser.parse("./../../../resources/main/yaml/BenchmarkExecution.yaml", BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse("./../../../resources/main/yaml/BenchmarkType.yaml", KubernetesBenchmark::class.java)!!

        val shutdown = Shutdown(benchmarkExecution, benchmark)
        Runtime.getRuntime().addShutdownHook(thread { shutdown.run()})

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
        logger.info { "Theodolite finished" }
        Runtime.getRuntime().removeShutdownHook(thread { shutdown.run()})
        exitProcess(0)
    }
}
