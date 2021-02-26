package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

class ReplicaPatcher(private val k8sResource: KubernetesResource): AbstractPatcher(k8sResource){
    override fun <Int> patch(value: Int) {
        if (k8sResource is Deployment) {
            if (value is kotlin.Int) {
                this.k8sResource.spec.replicas = value
            }
        }
    }
}