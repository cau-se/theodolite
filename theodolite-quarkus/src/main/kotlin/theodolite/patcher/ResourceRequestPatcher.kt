package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements
import io.fabric8.kubernetes.api.model.apps.Deployment

class ResourceRequestPatcher(
    private val k8sResource: KubernetesResource,
    private val container: String,
    private val variableName: String
) : AbstractPatcher(k8sResource, container, variableName) {

    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                try {
                    if (it.resources.requests.isEmpty()) {
                        it.resources.requests = mapOf(variableName to Quantity(value as kotlin.String))
                    } else {
                        val values = mutableMapOf<kotlin.String, Quantity>()
                        it.resources.requests.forEach { entry -> values.put(entry.key, entry.value) }
                        values[variableName] = Quantity(value as kotlin.String)
                        it.resources.requests = values
                    }
                } catch (e: IllegalStateException) {
                    val resource = ResourceRequirements()
                    resource.requests = mapOf(variableName to Quantity(value as kotlin.String))
                    it.resources = resource
                }
            }
        }
    }
}
