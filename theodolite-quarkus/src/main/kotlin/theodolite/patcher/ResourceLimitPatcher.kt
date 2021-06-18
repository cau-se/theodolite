package theodolite.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Resource limit [Patcher] set resource limits for deployments and statefulSets.
 *
 * @param k8sResource Kubernetes resource to be patched.
 * @param container Container to be patched.
 * @param limitedResource The resource to be limited (e.g. **cpu or memory**)
 */
class ResourceLimitPatcher(
    private val k8sResource: KubernetesResource,
    private val container: String,
    private val limitedResource: String
) : AbstractPatcher(k8sResource) {

    override fun <String> patch(value: String) {
        when (k8sResource) {
            is Deployment -> {
                k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setLimits(it, value as kotlin.String)
                }
            }
            is StatefulSet -> {
                k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setLimits(it, value as kotlin.String)
                }
            }
            else -> {
                throw IllegalArgumentException("ResourceLimitPatcher not applicable for $k8sResource")
            }
        }
    }

    private fun setLimits(container: Container, value: String) {
        when {
            container.resources == null -> {
                val resource = ResourceRequirements()
                resource.limits = mapOf(limitedResource to Quantity(value))
                container.resources = resource
            }
            container.resources.limits.isEmpty() -> {
                container.resources.limits = mapOf(limitedResource to Quantity(value))
            }
            else -> {
                val values = mutableMapOf<String, Quantity>()
                container.resources.limits.forEach { entry -> values[entry.key] = entry.value }
                values[limitedResource] = Quantity(value)
                container.resources.limits = values
            }
        }
    }
}
