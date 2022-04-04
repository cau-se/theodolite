package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * The Scheduler name [Patcher] make it possible to set the scheduler which should
 * be used to deploy the given deployment.
 * @param k8sResource Kubernetes resource to be patched.
 */
class SchedulerNamePatcher : AbstractPatcher() {


    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            resource.spec.template.spec.schedulerName = value
        }
        return resource
    }
}
