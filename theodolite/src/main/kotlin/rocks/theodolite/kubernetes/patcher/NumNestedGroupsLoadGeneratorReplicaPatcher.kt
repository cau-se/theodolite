package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.apps.Deployment
import kotlin.math.pow

class NumNestedGroupsLoadGeneratorReplicaPatcher(
    private val numSensors: String,
    private val loadGenMaxRecords: String,
) : AbstractPatcher() {

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        if (resource is Deployment) {
            val approxNumSensors = numSensors.toDouble().pow(Integer.parseInt(value).toDouble())
            val loadGenInstances =
                (approxNumSensors + loadGenMaxRecords.toDouble() - 1) / loadGenMaxRecords.toDouble()
            resource.spec.replicas = loadGenInstances.toInt()

        }
        return resource
    }
}

