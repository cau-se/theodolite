package theodolite

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.benchmark.TheodoliteYamlExecutor

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        //val theodolite = TheodoliteExecutor()
        val theodolite = TheodoliteYamlExecutor()
        theodolite.run()
        logger.info("Application started")
    }
}
