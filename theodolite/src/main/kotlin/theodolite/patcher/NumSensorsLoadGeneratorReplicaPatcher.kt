package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment


class NumSensorsLoadGeneratorReplicaPatcher(
    private val k8sResource: KubernetesResource,
    private val loadGenMaxRecords: String
) :
    AbstractPatcher(k8sResource) {
    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            if (value is kotlin.String) {
                val loadGenInstances = (Integer.parseInt(value) + loadGenMaxRecords.toInt() - 1) / loadGenMaxRecords.toInt()
                this.k8sResource.spec.replicas = loadGenInstances
            }
        }
    }
}
