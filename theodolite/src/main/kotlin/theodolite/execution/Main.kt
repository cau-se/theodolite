package theodolite.execution

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import theodolite.execution.operator.TheodoliteOperator
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val mode = System.getenv("MODE") ?: "standalone"
        logger.info { "Start Theodolite with mode $mode" }

        when (mode) {
            "standalone" -> TheodoliteStandalone().start()
            "yaml-executor" -> TheodoliteStandalone().start() // TODO remove (#209)
            "operator" -> TheodoliteOperator().start()
            else -> {
                logger.error { "MODE $mode not found" }
                exitProcess(1)
            }
        }
    }
}
