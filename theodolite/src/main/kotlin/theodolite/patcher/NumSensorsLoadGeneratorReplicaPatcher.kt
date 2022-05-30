package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment


class NumSensorsLoadGeneratorReplicaPatcher(
    private val loadGenMaxRecords: String,
) : AbstractPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            val loadGenInstances =
                (Integer.parseInt(value) + loadGenMaxRecords.toInt() - 1) / loadGenMaxRecords.toInt()
            resource.spec.replicas = loadGenInstances

        }
        return resource
    }

}
