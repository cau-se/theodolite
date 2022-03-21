package rocks.theodolite.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection


@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class Action {

    lateinit var selector: ActionSelector
    lateinit var exec: Command

    fun exec(client: NamespacedKubernetesClient) {
        val exitCode = ActionCommand(client = client)
            .exec(
                matchLabels = selector.pod.matchLabels,
                container = selector.container,
                timeout = exec.timeoutSeconds,
                command = exec.command
        )
            if(exitCode != 0){
            throw ActionCommandFailedException("Error while executing action, finished with exit code $exitCode")
        }
    }
}

@JsonDeserialize
@RegisterForReflection
class ActionSelector {
    lateinit var pod: PodSelector
    var container: String = ""
}
@JsonDeserialize
@RegisterForReflection
class PodSelector {
    lateinit var matchLabels: MutableMap<String, String>
}
@JsonDeserialize
@RegisterForReflection
class Command {
    lateinit var command: Array<String>
    var timeoutSeconds: Long = Configuration.TIMEOUT_SECONDS
}