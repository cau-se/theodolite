package rocks.theodolite.kubernetes

import io.quarkus.runtime.LaunchMode
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

@QuarkusMain
object Theodolite {

    @JvmStatic
    fun main(args: Array<String>) {
        Quarkus.run()
    }

    @ApplicationScoped
    class AppEventListener {

        @Inject
        private lateinit var launchMode: LaunchMode

        fun onStart(@Observes ev: StartupEvent) {
            if (launchMode == LaunchMode.TEST) {
                LOGGER.warn("Theodolite does not automatically start in TEST mode.")
            } else {
                val mode = Configuration.EXECUTION_MODE
                LOGGER.info { "Start Theodolite in $mode mode." }

                when (mode.lowercase()) {
                    ExecutionModes.OPERATOR.value -> Unit
                    else -> {
                        LOGGER.error { "MODE $mode not found" }
                        Quarkus.asyncExit()
                    }
                }
            }
        }

        fun onStop(@Observes ev: ShutdownEvent) {
            if (launchMode == LaunchMode.TEST) {
                LOGGER.warn("Theodolite did not automatically start in TEST mode.")
            } else {
                LOGGER.info("Quit Theodolite.")
            }
        }

    }

}
