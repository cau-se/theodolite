package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata

/**
 * The DataVolumeLoadGeneratorReplicaPatcher takes the total load that should be generated
 * and computes the number of instances needed for this load based on the `maxVolume`
 * ((load + maxVolume - 1) / maxVolume) and calculates the load per instance
 * (loadPerInstance = load / instances).
 * The number of instances are set for the load generator and the given variable is set to the
 * load per instance.
 *
 * @property maxVolume per load generator instance
 * @property container Container to be patched.
 * @property variableName Name of the environment variable to be patched.
 */
class DataVolumeLoadGeneratorReplicaPatcher(
    private val maxVolume: Int,
    val container: String,
    val variableName: String
) : AbstractStringPatcher() {

    private val envVarPatcher = EnvVarPatcher(container, variableName)
    private val replicaPatcher = ReplicaPatcher()

    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        // calculate number of load generator instances and load per instance
        val load = value.toInt()
        val loadGenInstances = (load + maxVolume - 1) / maxVolume
        val loadPerInstance = load / loadGenInstances

        // Patch instance values and load value of generators
        return this.envVarPatcher.patchSingleResource(
            replicaPatcher.patchSingleResource(resource, loadGenInstances),
            loadPerInstance.toString())
    }
}
