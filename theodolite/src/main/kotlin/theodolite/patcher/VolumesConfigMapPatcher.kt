package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class VolumesConfigMapPatcher(private var volumeName: String
) : AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            if (resource.spec.template.spec.volumes == null) {
                resource.spec.template.spec.volumes = mutableListOf()
            }
            val volumeMounts = resource.spec.template.spec.volumes

            for (mount in volumeMounts) {
                try {
                    if (mount.configMap.name == volumeName) {
                        mount.configMap.name = value
                    }
                } catch (_: NullPointerException) {
                }
            }
        }
        if (resource is StatefulSet) {
            if (resource.spec.template.spec.volumes == null) {
                resource.spec.template.spec.volumes = mutableListOf()
            }
            val volumeMounts = resource.spec.template.spec.volumes

            for (mount in volumeMounts) {
                try {
                    if (mount.configMap.name == volumeName) {
                        mount.configMap.name = value
                    }
                } catch (_: NullPointerException) {
                }
            }
        }

        return resource
    }
}