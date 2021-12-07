package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.ExecListener
import io.fabric8.kubernetes.client.dsl.ExecWatch
import mu.KotlinLogging
import okhttp3.Response
import theodolite.util.ActionCommandFailedException
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

private val logger = KotlinLogging.logger {}
private const val TIMEOUT = 30L

class ActionCommand(val client: NamespacedKubernetesClient) {
    var out: ByteArrayOutputStream = ByteArrayOutputStream()
    var error: ByteArrayOutputStream = ByteArrayOutputStream()
    private val execLatch = CountDownLatch(1);

    /**
     * Executes an action command.
     *
     * @param matchLabels matchLabels specifies on which pod the command should be executed. For this, the principle
     * `of any` of is used and the command is called on one of the possible pods.
     * @param container (Optional) The container to run the command. Is optional iff exactly one container exist.
     * @param command The command to be executed.
     * @return the exit code of this executed command
     */
    fun exec(matchLabels: MutableMap<String, String>, command: Array<String>, container: String = ""): Int {

        val exitCode = ExitCode()

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
                .usingListener(MyPodExecListener(execLatch, exitCode))
                .exec(*command)

            val latchTerminationStatus = execLatch.await(TIMEOUT, TimeUnit.SECONDS);
            if (!latchTerminationStatus) {
                throw ActionCommandFailedException("Latch could not terminate within specified time")
            }
            execWatch.close();
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt();
            throw ActionCommandFailedException("Interrupted while waiting for the exec", e)
        }

        logger.info { "Action command finished with code ${exitCode.code}" }
        return exitCode.code
    }

    private fun getPodName(matchLabels: MutableMap<String, String>, tries: Int): String {
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

    private class MyPodExecListener(val execLatch: CountDownLatch, val exitCode: ExitCode) : ExecListener {
        override fun onOpen(response: Response) {
        }

        override fun onFailure(throwable: Throwable, response: Response) {
            execLatch.countDown()
            throw ActionCommandFailedException("Some error encountered while executing action: ${throwable.printStackTrace()}")
        }

        override fun onClose(code: Int, reason: String) {
            exitCode.code = code
            exitCode.reason = reason
            execLatch.countDown()
        }
    }

    private class ExitCode() {
        var code by Delegates.notNull<Int>()
        lateinit var reason: String
    }
}
