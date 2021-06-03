package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import kotlin.math.pow

class NumNestedGroupsLoadGeneratorReplicaPatcher(
    private val k8sResource: KubernetesResource,
    private val numSensors: String,
    private val loadGenMaxRecords: String
    ) :
    AbstractPatcher(k8sResource) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            if (value is kotlin.String) {
                val approxNumSensors =  numSensors.toDouble().pow(Integer.parseInt(value).toDouble())
                val loadGenInstances = (approxNumSensors + loadGenMaxRecords.toDouble() - 1) / loadGenMaxRecords.toDouble()
                this.k8sResource.spec.replicas = loadGenInstances.toInt()
            }
        }
    }
}
