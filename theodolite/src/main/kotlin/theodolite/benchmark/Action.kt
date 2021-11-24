package theodolite.benchmark

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class Action {

    lateinit var selector: ActionSelector
    lateinit var exec: Command

    fun exec(client: NamespacedKubernetesClient) {
        ActionCommand(client = client)
            .exec(
                matchLabels = selector.pod.matchLabels,
                container = selector.container,
                command = exec.command
            )
    }
}

class ActionSelector {
    lateinit var pod: PodSelector
    lateinit var container: String
}

class PodSelector {
    lateinit var matchLabels: MutableMap<String, String>
}

class Command {
    lateinit var command: String
}