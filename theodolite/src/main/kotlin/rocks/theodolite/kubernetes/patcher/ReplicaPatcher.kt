package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Replica [Patcher] modifies the number of replicas for the given Kubernetes *Deployment* or *StatefulSet*.
 *
 */
class ReplicaPatcher : AbstractIntPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.replicas = value
            }
            is StatefulSet -> {
                resource.spec.replicas = value
            }
        }
        return resource
    }
}