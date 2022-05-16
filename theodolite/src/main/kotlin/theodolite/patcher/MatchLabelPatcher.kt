package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * This patcher is able to set the `spec.selector.matchLabels` for a `Deployment` or `StatefulSet` Kubernetes resource.
 *
 * @property variableName The matchLabel which should be set
 */
class MatchLabelPatcher(
    val variableName: String) :
    AbstractPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                if (resource.spec.selector.matchLabels == null) {
                    resource.spec.selector.matchLabels = mutableMapOf()
                }
                resource.spec.selector.matchLabels[this.variableName] = value
            }
            is StatefulSet -> {
                if (resource.spec.selector.matchLabels == null) {
                    resource.spec.selector.matchLabels = mutableMapOf()
                }
                resource.spec.selector.matchLabels[this.variableName] = value
            }
        }
        return resource
    }
}