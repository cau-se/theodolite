package theodolite.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

class LabelPatcher(
    val variableName: String) :
    AbstractPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value
            }
            is StatefulSet -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value
            }
            is Service -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value

            }
            is ConfigMap -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value
            }
            is GenericKubernetesResource -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value
            }
        }
        return resource
    }
}