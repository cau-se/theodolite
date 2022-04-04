package theodolite.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.utils.Serialization

class LabelPatcher(
    val variableName: String) :
    AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
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
            is CustomResource<*, *> -> {
                if (resource.metadata.labels == null) {
                    resource.metadata.labels = mutableMapOf()
                }
                resource.metadata.labels[this.variableName] = value
            }
        }
        return resource
    }
}