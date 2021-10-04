package theodolite.patcher

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
    k8sResource: KubernetesResource,
    private val maxVolume: Int,
    container: String,
    variableName: String
) : AbstractPatcher(k8sResource) {

    private val replicaPatcher = ReplicaPatcher(k8sResource)
    private val envVarPatcher = EnvVarPatcher(k8sResource, container, variableName)

    override fun <T> patch(value: T) {
        // calculate number of load generator instances and load per instance
        val load = Integer.parseInt(value.toString())
        val loadGenInstances = (load + maxVolume - 1) / maxVolume
        val loadPerInstance = load / loadGenInstances

        // Patch instance values and load value of generators
        replicaPatcher.patch(loadGenInstances.toString())
        envVarPatcher.patch(loadPerInstance.toString())
    }
}
