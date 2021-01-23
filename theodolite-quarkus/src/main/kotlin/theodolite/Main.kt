package theodolite
import io.quarkus.runtime.annotations.QuarkusMain
import theodolite.execution.TheodoliteExecutor
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val theodolite = TheodoliteExecutor()
        theodolite.run()
        logger.info("Application started")
    }
}
