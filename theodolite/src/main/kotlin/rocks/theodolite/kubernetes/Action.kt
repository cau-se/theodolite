package rocks.theodolite.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
class Action {

    @JsonProperty("exec")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var execCommand: ExecCommand? = null
    @JsonProperty("delete")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var deleteCommand: DeleteCommand? = null

    fun exec(client: KubernetesClient) {
        return if (execCommand != null) {
            execCommand?.exec(client) !!
        } else if (deleteCommand != null) {
            deleteCommand?.exec(client) !!
        } else {
            throw DeploymentFailedException("Could not execute action. The action type must either be 'exec' or 'delete'.")
        }
    }

}
