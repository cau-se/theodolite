package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.dsl.ExecWatch
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger {}

class ActionCommand(val client: KubernetesClient) {

    /**
     * Executes an action command.
     *
     * @param matchLabels matchLabels specifies on which pod the command should be executed. For this, the principle
     * `of any` of is used and the command is called on one of the possible pods.
     * @param command The command to be executed.
     * @param timeout (Optional) Timeout for running the command.
     * @param container (Optional) The container to run the command. Is optional iff exactly one container exist.
     * @return the exit code of this executed command
     */
    fun exec(
        matchLabels: Map<String, String>,
        command: Array<String>,
        timeout: Long = Configuration.TIMEOUT_SECONDS,
        container: String = ""
    ): Int {
        try {
            val outStream = ByteArrayOutputStream()
            val errorStream = ByteArrayOutputStream()
            val execWatch: ExecWatch = client.pods()
                .inNamespace(client.namespace)
                .withName(awaitPodName(matchLabels, 3))
                .let { if (container.isNotEmpty()) it.inContainer(container) else it }
                .writingOutput(outStream)
                .writingError(errorStream)
                .exec(*command)
            val exitCode = execWatch.exitCode().get(timeout, TimeUnit.SECONDS)
            execWatch.close()
            logger.debug { "Execution Output Stream is \n $outStream" }
            logger.debug { "Execution Error Stream is \n $errorStream" }
            return exitCode
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ActionCommandFailedException("Interrupted while waiting for the exec", e)
        } catch (e: KubernetesClientException) {
            throw ActionCommandFailedException("Error while executing command", e)
        }
    }

    /**
     * Find pod with matching labels. The matching pod must have the status `Running`.
     *
     * @param matchLabels the match labels
     * @param tries specifies the number of times to look for a  matching pod. When pods are newly created,
     * it can take a while until the status is ready and the pod can be selected.
     * @return the name of the pod or throws [ActionCommandFailedException]
     */
    fun awaitPodName(matchLabels: Map<String, String>, tries: Int): String {
        for (i in 1..tries) {
            try {
                return getPodName(matchLabels)
            } catch (e: Exception) {
                logger.warn { "Could not found any pod with specified matchlabels or pod is not ready." }
            }
            Thread.sleep(Duration.ofSeconds(5).toMillis())
        }
        throw ActionCommandFailedException("Couldn't find any pod that matches the specified labels.")
    }

    private fun getPodName(matchLabels: Map<String, String>): String {
        return try {
            val podNames = this.client
                .pods()
                .withLabels(matchLabels)
                .list()
                .items
                .map { it.metadata.name }

            podNames.first {
                this.client.pods().withName(it).isReady
            }

        } catch (e: NoSuchElementException) {
            throw ActionCommandFailedException("Couldn't find any pod that matches the specified labels.", e)
        }
    }

}
