package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata

class LabelPatcher(
    val labelName: String) :
    AbstractStringPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource.metadata.labels == null) {
            resource.metadata.labels = mutableMapOf()
        }
        resource.metadata.labels[this.labelName] = value
        return resource
    }
}