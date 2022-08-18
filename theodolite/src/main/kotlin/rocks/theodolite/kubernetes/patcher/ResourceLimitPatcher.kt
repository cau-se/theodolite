package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements

/**
 * The Resource limit [Patcher] set resource limits for deployments and statefulSets.
 * The Resource limit [Patcher] sets resource limits for Deployments and StatefulSets.
 *
 * @param container Container to be patched.
 * @param limitedResource The resource to be limited (e.g., **cpu** or **memory**)
 * @param format Format add to the provided value (e.g., `GBi` or `m`, see [Quantity]).
 * @param factor A factor to multiply the provided value with.
 */
class ResourceLimitPatcher(
    container: String,
    limitedResource: String,
    format: String? = null,
    factor: Int? = null
) : AbstractResourcePatcher(
    container = container,
    requiredResource = limitedResource,
    format = format,
    factor = factor
) {
    override fun setLimits(container: Container, quantity: Quantity) {
        when {
            container.resources == null -> {
                val resource = ResourceRequirements()
                resource.limits = mapOf(requiredResource to quantity)
                container.resources = resource
            }
            container.resources.limits.isEmpty() -> {
                container.resources.limits = mapOf(requiredResource to quantity)
            }
            else -> {
                container.resources.limits[requiredResource] = quantity
            }
        }
    }

}
