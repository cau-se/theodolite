package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class VolumesConfigMapPatcher(private var volumeName: String) : AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        val volumeMounts = when(resource) {
            is Deployment -> {
                if (resource.spec.template.spec.volumes == null) {
                    resource.spec.template.spec.volumes = mutableListOf()
                }
                resource.spec.template.spec.volumes
            }
            is StatefulSet -> {
                if (resource.spec.template.spec.volumes == null) {
                    resource.spec.template.spec.volumes = mutableListOf()
                }
                resource.spec.template.spec.volumes
            }
            else -> emptyList() // No volumes to patch
        }

        for (mount in volumeMounts) {
            // Find ConfigMap volume with requested name
            if (mount.configMap?.name?.equals(volumeName) == true) {
                mount.configMap.name = value
            }
        }
        return resource
    }
}