package theodolite.execution

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser
import java.nio.file.Paths
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteYamlExecutor")
object TheodoliteYamlExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Theodolite started" }
        val path = Paths.get("").toAbsolutePath().toString()
        logger.info{ path }

        // load the BenchmarkExecution and the BenchmarkType
        val parser = YamlParser()
        val benchmarkExecution =
            parser.parse("./config/BenchmarkExecution.yaml", BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse("./config/BenchmarkType.yaml", KubernetesBenchmark::class.java)!!

        logger.info { benchmark.name.toString() }

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
        logger.info { "Theodolite finished" }
        exitProcess(0)
    }
}
