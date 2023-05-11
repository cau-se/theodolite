package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Scheduler name [Patcher] make it possible to set the scheduler which should
 * be used to deploy the given Deployment.
 */
class SchedulerNamePatcher : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.schedulerName = value
            }
            is StatefulSet -> {
                resource.spec.template.spec.schedulerName = value
            }
            is Pod -> {
                resource.spec.schedulerName = value
            }
        }
        return resource
    }
}
