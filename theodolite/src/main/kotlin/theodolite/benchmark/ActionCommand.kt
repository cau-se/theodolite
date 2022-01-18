package theodolite.benchmark

import io.fabric8.kubernetes.api.model.Status
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.ExecListener
import io.fabric8.kubernetes.client.dsl.ExecWatch
import io.fabric8.kubernetes.client.utils.Serialization
import mu.KotlinLogging
import okhttp3.Response
import theodolite.util.ActionCommandFailedException
import theodolite.util.Configuration
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger {}

class ActionCommand(val client: NamespacedKubernetesClient) {
    var out: ByteArrayOutputStream = ByteArrayOutputStream()
    var error: ByteArrayOutputStream = ByteArrayOutputStream()
    var errChannelStream: ByteArrayOutputStream = ByteArrayOutputStream()
    private val execLatch = CountDownLatch(1)

    /**
     * Executes an action command.
     *
     * @param matchLabels matchLabels specifies on which pod the command should be executed. For this, the principle
     * `of any` of is used and the command is called on one of the possible pods.
     * @param container (Optional) The container to run the command. Is optional iff exactly one container exist.
     * @param command The command to be executed.
     * @return the exit code of this executed command
     */
    fun exec(
        matchLabels: MutableMap<String, String>,
        command: Array<String>,
        timeout: Long = Configuration.TIMEOUT_SECONDS,
        container: String = ""
    ): Int {
        try {
            val execWatch: ExecWatch = if (container.isNotEmpty()) {
                client.pods()
                    .inNamespace(client.namespace)
                    .withName(getPodName(matchLabels, 3))
                    .inContainer(container)

            } else {
                client.pods()
                    .inNamespace(client.namespace)
                    .withName(getPodName(matchLabels, 3))
            }
                .writingOutput(out)
                .writingError(error)
                .writingErrorChannel(errChannelStream)
                .usingListener(ActionCommandListener(execLatch))
                .exec(*command)

            val latchTerminationStatus = execLatch.await(timeout, TimeUnit.SECONDS)
            if (!latchTerminationStatus) {
                throw ActionCommandFailedException("Latch could not terminate within specified time")
            }
            execWatch.close()
        } catch (e: Exception) {
            when (e) {
                is InterruptedException -> {
                    Thread.currentThread().interrupt()
                    throw ActionCommandFailedException("Interrupted while waiting for the exec", e)
                }
                is KubernetesClientException -> {
                    throw ActionCommandFailedException("Error while executing command", e)
                }
                else -> {
                    throw e
                }
            }
        }
        logger.debug { "Execution Output Stream is \n $out" }
        logger.debug { "Execution Error Stream is \n $error" }
        logger.debug { "Execution ErrorChannel is: \n $errChannelStream" }
        return getExitCode(errChannelStream)
    }

    private fun getExitCode(errChannelStream: ByteArrayOutputStream): Int {
        val status: Status?
        try {
            status = Serialization.unmarshal(errChannelStream.toString(), Status::class.java)
        } catch (e: Exception) {
            throw ActionCommandFailedException("Could not determine the exit code, no information given")
        }

        if (status == null) {
            throw ActionCommandFailedException("Could not determine the exit code, no information given")
        }

        return if (status.status.equals("Success")) {
            0
        } else status.details.causes.stream()
            .filter { it.reason.equals("ExitCode") }
            .map { it.message }
            .findFirst()
            .orElseThrow {
                ActionCommandFailedException("Status is not SUCCESS but contains no exit code - Status: $status")
            }.toInt()
    }

    /**
     * Find pod with matching labels. The matching pod must have the status `Running`.
     *
     * @param matchLabels the match labels
     * @param tries specifies the number of times to look for a  matching pod. When pods are newly created,
     * it can take a while until the status is ready and the pod can be selected.
     * @return the name of the pod or throws [ActionCommandFailedException]
     */
    fun getPodName(matchLabels: MutableMap<String, String>, tries: Int): String {
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

    private fun getPodName(matchLabels: MutableMap<String, String>): String {
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

    private class ActionCommandListener(val execLatch: CountDownLatch) : ExecListener {
        override fun onOpen(response: Response) {
        }

        override fun onFailure(throwable: Throwable, response: Response) {
            execLatch.countDown()
            throw ActionCommandFailedException("Some error encountered while executing action, caused ${throwable.message})")
        }

        override fun onClose(code: Int, reason: String) {
            execLatch.countDown()
        }
    }

}
