package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Replica patcher modifies the number of replicas for the given Kubernetes Deployment.
 *
 * @param k8sResource  Kubernetes resource to be patched.
 */
class ReplicaPatcher(private val k8sResource: KubernetesResource) : AbstractPatcher(k8sResource) {
    override fun <Int> patch(value: Int) {
        if (k8sResource is Deployment) {
            if (value is kotlin.Int) {
                this.k8sResource.spec.replicas = value
            }
        }
    }
}
