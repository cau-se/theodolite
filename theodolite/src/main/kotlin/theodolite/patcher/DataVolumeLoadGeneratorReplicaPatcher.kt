package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment

/**
 * The DataVolumeLoadGeneratorReplicaPatcher allows to modify the value of an environment variable
 *
 * @property k8sResource Kubernetes resource to be patched.
 * @property container Container to be patched.
 * @property variableName Name of the environment variable to be patched.
 */
class DataVolumeLoadGeneratorReplicaPatcher(
    private val k8sResource: KubernetesResource,
    private val maxVolume: String,
    container: String,
    variableName: String
) : EnvVarPatcher(k8sResource, container, variableName) {

    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            if (value is kotlin.String) {
                val load = value.toInt()
                val loadGenInstances = (load + maxVolume.toInt() - 1) / maxVolume.toInt()
                this.k8sResource.spec.replicas = loadGenInstances
                val loadPerInstance = load / loadGenInstances
                super.patch(loadPerInstance.toString())
            }
        }
    }
}
