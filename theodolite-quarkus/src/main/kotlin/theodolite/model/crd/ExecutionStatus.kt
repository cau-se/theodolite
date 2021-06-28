package theodolite.model.crd

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced

@JsonDeserialize
class ExecutionStatus(): KubernetesResource, Namespaced {
    var executionState: String = ""
    var executionDuration: String = "-"
}