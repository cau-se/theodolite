package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import rocks.theodolite.kubernetes.operator.TheodoliteOperator
import rocks.theodolite.kubernetes.standalone.TheodoliteStandalone
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@QuarkusMain
object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val mode = Configuration.EXECUTION_MODE
        logger.info { "Start Theodolite with mode $mode" }

        val namespace = Configuration.NAMESPACE
        val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(namespace)

        when (mode.lowercase()) {
            ExecutionModes.STANDALONE.value -> TheodoliteStandalone(client).start()
            ExecutionModes.OPERATOR.value -> TheodoliteOperator(client).start()
            else -> {
                logger.error { "MODE $mode not found" }
                exitProcess(1)
            }
        }
    }
}
