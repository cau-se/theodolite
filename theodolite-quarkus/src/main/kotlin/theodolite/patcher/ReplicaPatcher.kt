package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

class ReplicaPatcher(private val k8sResource: KubernetesResource): AbstractPatcher<Int>(k8sResource){
    override fun patch(replicas: Int) {
        if (k8sResource is Deployment)
            this.k8sResource.spec.replicas = replicas
    }
}