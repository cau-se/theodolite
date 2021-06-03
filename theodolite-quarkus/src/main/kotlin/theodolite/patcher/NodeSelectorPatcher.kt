package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Node selector patcher make it possible to set the NodeSelector of a Kubernetes deployment.
 *
 * @param k8sResource Kubernetes resource to be patched.
 * @param variableName The `label-key` of the node for which the `label-value` is to be patched.
 */
class NodeSelectorPatcher(private val k8sResource: KubernetesResource, private val variableName: String) :
    AbstractPatcher(k8sResource) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.nodeSelector = mapOf(variableName to value as kotlin.String)
        }
    }
}
