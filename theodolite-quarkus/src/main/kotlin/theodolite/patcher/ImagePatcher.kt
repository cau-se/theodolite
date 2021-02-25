package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class ImagePatcher(private val k8sResource: KubernetesResource, private val container: String, private val variableName: String): AbstractPatcher(k8sResource, container, variableName) {

    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                it.image = value as kotlin.String
            }
        } else if (k8sResource is StatefulSet) {
            k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                it.image = value as kotlin.String
            }
        }
    }
}