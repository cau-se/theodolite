package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * This patcher is able to set the `spec.selector.matchLabels` for a `Deployment` or `StatefulSet` Kubernetes resource.
 *
 * @property k8sResource The Kubernetes manifests to patch
 * @property variableName The matchLabel which should be set
 */
class MatchLabelPatcher(private val k8sResource: KubernetesResource, val variableName: String) :
    AbstractPatcher(k8sResource) {

    override fun <String> patch(labelValue: String) {
        if (labelValue is kotlin.String) {
            when (k8sResource) {
                is Deployment -> {
                    if (k8sResource.spec.selector.matchLabels == null) {
                        k8sResource.spec.selector.matchLabels = mutableMapOf()
                    }
                    k8sResource.spec.selector.matchLabels[this.variableName] = labelValue
                }
                is StatefulSet -> {
                    if (k8sResource.spec.selector.matchLabels == null) {
                        k8sResource.spec.selector.matchLabels = mutableMapOf()
                    }
                    k8sResource.spec.selector.matchLabels[this.variableName] = labelValue
                }
            }
        }
    }
}