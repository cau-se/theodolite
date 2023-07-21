package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Node selector patcher make it possible to set the NodeSelector of a Kubernetes deployment.
 *
 * @param nodeLabelName The `label-key` of the node for which the `label-value` is to be patched.
 */
class NodeSelectorPatcher(private val nodeLabelName: String) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        val nodeSelector = mapOf(nodeLabelName to value)
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.nodeSelector = nodeSelector
            }
            is StatefulSet -> {
                resource.spec.template.spec.nodeSelector = nodeSelector
            }
            is Pod -> {
                resource.spec.nodeSelector = nodeSelector
            }
        }
        return resource
    }

}
