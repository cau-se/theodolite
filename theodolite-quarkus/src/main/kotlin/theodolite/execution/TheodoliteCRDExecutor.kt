package theodolite.execution

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.runtime.annotations.QuarkusMain
import mu.KotlinLogging
import kotlin.system.exitProcess

private var DEFAULT_NAMESPACE = "default"
private val logger = KotlinLogging.logger {}

@QuarkusMain(name = "TheodoliteCRDExecutor")
object TheodoliteCRDExecutor {
    @JvmStatic
    fun main(args: Array<String>) {

        val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        logger.info { "Using $namespace as namespace." }

        val client = DefaultKubernetesClient().inNamespace(namespace)

        println("hello there")

        exitProcess(0)
    }
}
