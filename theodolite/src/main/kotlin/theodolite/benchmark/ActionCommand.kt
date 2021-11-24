package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.ExecListener
import io.fabric8.kubernetes.client.dsl.ExecWatch
import mu.KotlinLogging
import okhttp3.Response
import theodolite.util.ActionCommandFailedException
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

class ActionCommand(val client: NamespacedKubernetesClient) {
    var out: ByteArrayOutputStream = ByteArrayOutputStream()
    var error: ByteArrayOutputStream = ByteArrayOutputStream()
    private val execLatch = CountDownLatch(1);

    fun exec(matchLabels: MutableMap<String, String>, container: String, command: String): Pair<String, String> {
        try {
            val execWatch: ExecWatch = client.pods()
                .withName(getPodName(matchLabels))
                .inContainer(container)
                .writingOutput(out)
                .writingError(error)
                .usingListener(MyPodExecListener(execLatch))
                .exec(*command.split(" ").toTypedArray())

            val latchTerminationStatus = execLatch.await(5, TimeUnit.SECONDS);
            if (!latchTerminationStatus) {
                logger.warn("Latch could not terminate within specified time");
            }
            execWatch.close();
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt();
            throw ActionCommandFailedException("Interrupted while waiting for the exec", e)
        }
        return Pair(out.toString(), error.toString())
    }

    private fun getPodName(matchLabels: MutableMap<String, String>): String {
        return try {
            this.client
                .pods()
                .withLabels(matchLabels)
                .list()
                .items
                .first()
                .metadata
                .name
        } catch (e: Exception) {
            throw ActionCommandFailedException("Couldn't find any pod that matches the specified labels.", e)
        }
    }

    private class MyPodExecListener(val execLatch: CountDownLatch) : ExecListener {
        override fun onOpen(response: Response) {
        }

        override fun onFailure(throwable: Throwable, response: Response) {
            logger.warn("Some error encountered while executing action")
            execLatch.countDown()
        }

        override fun onClose(i: Int, s: String) {
            execLatch.countDown()
        }
    }
}
