package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import kotlin.math.pow

private const val NUM_SENSORS = 4.0
private const val LOAD_GEN_MAX_RECORDS = 150000

class NumNestedGroupsLoadGeneratorReplicaPatcher(private val k8sResource: KubernetesResource) :
    AbstractPatcher(k8sResource) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            if (value is kotlin.String) {
                val approxNumSensors = NUM_SENSORS.pow(Integer.parseInt(value).toDouble())
                val loadGenInstances = (approxNumSensors + LOAD_GEN_MAX_RECORDS - 1) / LOAD_GEN_MAX_RECORDS
                this.k8sResource.spec.replicas = loadGenInstances.toInt()
            }
        }
    }
}
