package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource

/**
 * The DataVolumeLoadGeneratorReplicaPatcher takes the total load that should be generated
 * and computes the number of instances needed for this load based on the `maxVolume`
 * ((load + maxVolume - 1) / maxVolume) and calculates the load per instance
 * (loadPerInstance = load / instances).
 * The number of instances are set for the load generator and the given variable is set to the
 * load per instance.
 *
 * @property k8sResource Kubernetes resource to be patched.
 * @property maxVolume per load generator instance
 * @property container Container to be patched.
 * @property variableName Name of the environment variable to be patched.
 */
class DataVolumeLoadGeneratorReplicaPatcher(
    private val maxVolume: Int,
    private val container: String,
    private val variableName: String
) : Patcher {

    override fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata> {
        return resources.flatMap { patchSingeResource(it, value)}
    }

    fun patchSingeResource(k8sResource: HasMetadata, value: String): List<HasMetadata> {
        var resource = k8sResource
        // calculate number of load generator instances and load per instance
        val load = Integer.parseInt(value)
        val loadGenInstances = (load + maxVolume - 1) / maxVolume
        val loadPerInstance = load / loadGenInstances

        // Patch instance values and load value of generators
        val resourceList = ReplicaPatcher().patch(listOf(resource), loadGenInstances.toString())
        return EnvVarPatcher(this.container, this.variableName).patch(resourceList, loadPerInstance.toString())
    }
}
