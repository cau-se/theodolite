package theodolite.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.CustomResource

class LabelPatcher(private val k8sResource: KubernetesResource, val variableName: String) :
    AbstractPatcher(k8sResource) {

    override fun <String> patch(labelValue: String) {
        if(labelValue is kotlin.String){
            when(k8sResource){
                is Deployment -> {
                    if (k8sResource.metadata.labels == null){
                        k8sResource.metadata.labels = mutableMapOf()
                    }
                    k8sResource.metadata.labels[this.variableName] = labelValue
                }
                is StatefulSet -> {
                    if (k8sResource.metadata.labels == null){
                        k8sResource.metadata.labels = mutableMapOf()
                    }
                    k8sResource.metadata.labels[this.variableName] = labelValue
                }
                is Service -> {
                    if (k8sResource.metadata.labels == null){
                        k8sResource.metadata.labels = mutableMapOf()
                    }
                    k8sResource.metadata.labels[this.variableName] = labelValue
                }
                is ConfigMap -> {
                    if (k8sResource.metadata.labels == null){
                        k8sResource.metadata.labels = mutableMapOf()
                    }
                    k8sResource.metadata.labels[this.variableName] = labelValue
                }
                is CustomResource<*,*> -> {
                    if (k8sResource.metadata.labels == null){
                        k8sResource.metadata.labels = mutableMapOf()
                    }
                    k8sResource.metadata.labels[this.variableName] = labelValue
                }
            }
        }
    }
}