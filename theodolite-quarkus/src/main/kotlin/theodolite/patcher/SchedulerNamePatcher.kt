package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Scheduler name [Patcher] make it possible to set the scheduler which should
 * be used to deploy the given deployment.
 * @param k8sResource Kubernetes resource to be patched.
 */
class SchedulerNamePatcher(private val k8sResource: KubernetesResource) : Patcher {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.schedulerName = value as kotlin.String
        }
    }
}
