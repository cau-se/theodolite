package rocks.theodolite.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class ExecCommand {
    lateinit var selector: ExecActionSelector
    lateinit var command: Array<String>
    var timeoutSeconds: Long = Configuration.TIMEOUT_SECONDS
    fun exec(client: NamespacedKubernetesClient) {
        val exitCode = ActionCommand(client = client)
            .exec(
                matchLabels = selector.pod.matchLabels,
                container = selector.container,
                timeout = timeoutSeconds,
                command = command
            )
        if (exitCode != 0) {
            throw ActionCommandFailedException("Error while executing action, finished with exit code $exitCode")
        }
    }
}

@JsonDeserialize
@RegisterForReflection
class ExecActionSelector {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    lateinit var pod: PodSelector
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var container: String = ""
}
@JsonDeserialize
@RegisterForReflection
class PodSelector {
    lateinit var matchLabels: Map<String, String>
}