package theodolite.patcher

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import theodolite.util.InvalidPatcherConfigurationException

/**
 * The Resource request [Patcher] set resource limits for deployments and statefulSets.
 *
 * @param container Container to be patched.
 * @param requestedResource The resource to be requested (e.g. **cpu or memory**)
 */
class ResourceRequestPatcher(
    private val container: String,
    private val requestedResource: String
) : AbstractPatcher() {


    override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setRequests(it, value)
                }
            }
            is StatefulSet -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setRequests(it, value)
                }
            }
            else -> {
                throw InvalidPatcherConfigurationException("ResourceRequestPatcher is not applicable for $resource")
            }
        }
        return resource
    }
    private fun setRequests(container: Container, value: String) {
        when {
            container.resources == null -> {
                val resource = ResourceRequirements()
                resource.requests = mapOf(requestedResource to Quantity(value))
                container.resources = resource
            }
            container.resources.requests.isEmpty() -> {
                container.resources.requests = mapOf(requestedResource to Quantity(value))
            }
            else -> {
                val values = mutableMapOf<String, Quantity>()
                container.resources.requests.forEach { entry -> values[entry.key] = entry.value }
                values[requestedResource] = Quantity(value)
                container.resources.requests = values
            }
        }
    }
}
