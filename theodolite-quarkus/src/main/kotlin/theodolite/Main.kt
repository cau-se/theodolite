package theodolite

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.execution.TheodoliteYamlExecutor
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Theodolite started" }
        TheodoliteYamlExecutor().run()
        logger.info { "Theodolite finished" }
        exitProcess(0)
    }
}
