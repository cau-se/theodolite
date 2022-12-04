package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment


class NumSensorsLoadGeneratorReplicaPatcher(
    private val loadGenMaxRecords: Int
) : AbstractIntPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata {
        if (resource is Deployment) {
            resource.spec.replicas = (value + loadGenMaxRecords - 1) / loadGenMaxRecords

        }
        return resource
    }

}
