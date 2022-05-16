package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Node selector patcher make it possible to set the NodeSelector of a Kubernetes deployment.
 *
 * @param variableName The `label-key` of the node for which the `label-value` is to be patched.
 */
class NodeSelectorPatcher(
    private val variableName: String) :
    AbstractPatcher() {


    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            resource.spec.template.spec.nodeSelector = mapOf(variableName to value)
        }
        return resource
    }
}
