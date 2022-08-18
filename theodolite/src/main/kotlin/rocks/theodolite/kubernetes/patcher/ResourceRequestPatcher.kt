package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements


/**
 * The Resource request [Patcher] sets resource requests for Deployments and StatefulSets.
 *
 * @param container Container to be patched.
 * @param requestedResource The resource to be requested (e.g., **cpu** or **memory**)
 * @param format Format add to the provided value (e.g., `GBi` or `m`, see [Quantity]).
 * @param factor A factor to multiply the provided value with.
 */
class ResourceRequestPatcher(
    container: String,
    requestedResource: String,
    format: String? = null,
    factor: Int? = null
) : AbstractResourcePatcher(
    container = container,
    requiredResource = requestedResource,
    format = format,
    factor = factor
) {

    override fun setLimits(container: Container, quantity: Quantity) {
        when {
            container.resources == null -> {
                val resource = ResourceRequirements()
                resource.requests = mapOf(requiredResource to quantity)
                container.resources = resource
            }
            container.resources.requests.isEmpty() -> {
                container.resources.requests = mapOf(requiredResource to quantity)
            }
            else -> {
                container.resources.requests[requiredResource] = quantity
            }
        }
    }

}
