package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The Replica [Patcher] modifies the number of replicas for the given Kubernetes deployment.
 *
 */
class ReplicaPatcher : AbstractIntPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata {
        if (resource is Deployment) {
            resource.spec.replicas = value
        }
        return resource
    }
}