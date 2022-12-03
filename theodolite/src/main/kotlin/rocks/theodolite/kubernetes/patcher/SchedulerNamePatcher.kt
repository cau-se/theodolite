package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Scheduler name [Patcher] make it possible to set the scheduler which should
 * be used to deploy the given deployment.
 */
class SchedulerNamePatcher : AbstractPatcher() {


    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            resource.spec.template.spec.schedulerName = value
        }
        return resource
    }
}
