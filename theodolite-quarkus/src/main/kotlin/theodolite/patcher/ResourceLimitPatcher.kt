package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements
import io.fabric8.kubernetes.api.model.apps.Deployment
import javax.validation.constraints.Null

class ResourceLimitPatcher(
    private val k8sResource: KubernetesResource,
    private val container: String,
    private val limitedResource: String
) : AbstractPatcher(k8sResource, container, limitedResource) {

    override fun <String> patch(value: String) {
        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                when {
                    it.resources == null -> {
                        val resource = ResourceRequirements()
                        resource.limits = mapOf(limitedResource to Quantity(value as kotlin.String))
                        it.resources = resource
                    }
                    it.resources.limits.isEmpty() -> {
                        it.resources.limits = mapOf(limitedResource to Quantity(value as kotlin.String))
                    }
                    else -> {
                        val values = mutableMapOf<kotlin.String, Quantity>()
                        it.resources.limits.forEach { entry -> values[entry.key] = entry.value }
                        values[limitedResource] = Quantity(value as kotlin.String)
                        it.resources.limits = values
                    }
                }
            }
        }
    }
}
