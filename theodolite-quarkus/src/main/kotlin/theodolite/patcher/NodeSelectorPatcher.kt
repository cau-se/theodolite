package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

class NodeSelectorPatcher(private val k8sResource: KubernetesResource, private val variableName: String) :
    AbstractPatcher(k8sResource, variableName) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.nodeSelector = mapOf(variableName to value as kotlin.String)
        }
    }
}
