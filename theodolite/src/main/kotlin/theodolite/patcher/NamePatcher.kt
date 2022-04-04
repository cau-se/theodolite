package theodolite.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class NamePatcher : AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.metadata.name = value
            }
            is StatefulSet -> {
                resource.metadata.name = value
            }
            is Service -> {
                resource.metadata.name = value
            }
            is ConfigMap -> {
                resource.metadata.name = value
            }
            is io.fabric8.kubernetes.api.model.networking.v1.Ingress -> {
                resource.metadata.name = value
            }
            is GenericKubernetesResource -> {
                resource.metadata.name = value
            }
        }
        return resource
    }
}