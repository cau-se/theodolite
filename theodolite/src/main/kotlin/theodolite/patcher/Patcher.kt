package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * A patcher can be used to modify values of Kubernetes resource.
 *
 * @constructor Create empty Patcher
 */
@RegisterForReflection
interface Patcher {
    /**
     * The patch method modifies a value in the definition of a
     * Kubernetes resource.
     *
     * @param T The type of value
     * @param value The value to be used.
     */
    fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata>
}
