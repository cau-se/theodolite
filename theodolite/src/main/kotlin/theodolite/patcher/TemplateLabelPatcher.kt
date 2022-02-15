package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class TemplateLabelPatcher(private val k8sResource: KubernetesResource, val variableName: String) :
    AbstractPatcher(k8sResource) {

    override fun <String> patch(labelValue: String) {
        if (labelValue is kotlin.String) {
            when (k8sResource) {
                is Deployment -> {
                    if (k8sResource.spec.template.metadata.labels == null) {
                        k8sResource.spec.template.metadata.labels = mutableMapOf()
                    }
                    k8sResource.spec.template.metadata.labels[this.variableName] = labelValue
                }
                is StatefulSet -> {
                    if (k8sResource.spec.template.metadata.labels == null) {
                        k8sResource.spec.template.metadata.labels = mutableMapOf()
                    }
                    k8sResource.spec.template.metadata.labels[this.variableName] = labelValue
                }
            }
        }
    }
}