package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * The Replica [Patcher] modifies the number of replicas for the given Kubernetes deployment.
 *
 */
class ReplicaPatcher : AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            resource.spec.replicas = Integer.parseInt(value)
        }
        return resource
    }
}