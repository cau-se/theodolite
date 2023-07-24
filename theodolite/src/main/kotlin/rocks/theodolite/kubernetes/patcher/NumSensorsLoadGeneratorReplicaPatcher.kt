package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet


class NumSensorsLoadGeneratorReplicaPatcher(
    private val loadGenMaxRecords: Int
) : AbstractIntPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata {
        val replicas = (value + loadGenMaxRecords - 1) / loadGenMaxRecords
        when (resource) {
            is Deployment -> {
                resource.spec.replicas = replicas
            }
            is StatefulSet -> {
                resource.spec.replicas = replicas
            }
        }
        return resource
    }

}
