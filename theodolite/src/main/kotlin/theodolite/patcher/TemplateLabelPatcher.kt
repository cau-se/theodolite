package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.utils.Serialization

/**
 * This patcher is able to set the field `spec.template.metadata.labels` for a `Deployment` or `StatefulSet` Kubernetes resource.
 *
 * @property variableName The label which should be set
 */
class TemplateLabelPatcher(
    val variableName: String) :
    AbstractPatcher() {


    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                if (resource.spec.template.metadata.labels == null) {
                    resource.spec.template.metadata.labels = mutableMapOf()
                }
                resource.spec.template.metadata.labels[this.variableName] = value
            }
            is StatefulSet -> {
                if (resource.spec.template.metadata.labels == null) {
                    resource.spec.template.metadata.labels = mutableMapOf()
                }
                resource.spec.template.metadata.labels[this.variableName] = value
            }
        }
        return resource
    }
}