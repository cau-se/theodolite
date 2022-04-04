package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * The Node selector patcher make it possible to set the NodeSelector of a Kubernetes deployment.
 *
 * @param k8sResource Kubernetes resource to be patched.
 * @param variableName The `label-key` of the node for which the `label-value` is to be patched.
 */
class NodeSelectorPatcher(
    private val variableName: String) :
    AbstractPatcher() {


    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            resource.spec.template.spec.nodeSelector = mapOf(variableName to value)
        }
        return resource
    }
}
