package theodolite

import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Application started")

        val x = DeploymentManager()
        x.printFile()
        //Quarkus.run()
    }
}
