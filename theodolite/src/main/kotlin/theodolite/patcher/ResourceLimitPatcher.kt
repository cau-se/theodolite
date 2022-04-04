package theodolite.patcher

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import theodolite.util.InvalidPatcherConfigurationException

/**
 * The Resource limit [Patcher] set resource limits for deployments and statefulSets.
 *
 * @param k8sResource Kubernetes resource to be patched.
 * @param container Container to be patched.
 * @param limitedResource The resource to be limited (e.g. **cpu or memory**)
 */
class ResourceLimitPatcher(
    private val container: String,
    private val limitedResource: String
) : AbstractPatcher() {

    override fun patchSingeResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setLimits(it, value)
                }
            }
            is StatefulSet -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setLimits(it, value)
                }
            }
            else -> {
                throw InvalidPatcherConfigurationException("ResourceLimitPatcher is not applicable for $resource")
            }
        }
        return resource
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
