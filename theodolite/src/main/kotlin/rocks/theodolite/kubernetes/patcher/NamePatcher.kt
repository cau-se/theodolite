package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class NamePatcher : AbstractPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        resource.metadata.name = value
        return resource
    }
}