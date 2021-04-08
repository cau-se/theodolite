package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

class ReplicaPatcher(private val k8sResource: KubernetesResource) : AbstractPatcher(k8sResource) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            if (value is kotlin.String) {
                this.k8sResource.spec.replicas = Integer.parseInt(value)
            }
        }
    }
}
