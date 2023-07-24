package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import kotlin.math.pow

class NumNestedGroupsLoadGeneratorReplicaPatcher(
    private val numSensors: Int,
    private val loadGenMaxRecords: Int,
) : AbstractIntPatcher() {

    private val replicaPatcher = ReplicaPatcher()

    override fun patchSingleResource(resource: HasMetadata, value: Int): HasMetadata {
        val approxNumSensors = numSensors.toDouble().pow(value.toDouble())
        val replicas =
                ((approxNumSensors + loadGenMaxRecords.toDouble() - 1) / loadGenMaxRecords.toDouble())
                        .toInt()
        return this.replicaPatcher.patchSingleResource(resource, replicas)
    }
}

