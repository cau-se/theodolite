package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata

class LabelPatcher(
    val variableName: String) :
    AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource.metadata.labels == null) {
            resource.metadata.labels = mutableMapOf()
        }
        resource.metadata.labels[this.variableName] = value
        return resource
    }
}