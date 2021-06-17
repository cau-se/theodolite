package theodolite.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet

/**
 * The Resource request [Patcher] set resource limits for deployments and statefulSets.
 *
 * @param k8sResource Kubernetes resource to be patched.
 * @param container Container to be patched.
 * @param requestedResource The resource to be requested (e.g. **cpu or memory**)
 */
class ResourceRequestPatcher(
    private val k8sResource: KubernetesResource,
    private val container: String,
    private val requestedResource: String
) : AbstractPatcher(k8sResource) {

    override fun <String> patch(value: String) {
        when (k8sResource) {
            is Deployment -> {
                k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setRequests(it, value as kotlin.String)
                }
            }
            is StatefulSet -> {
                k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setRequests(it, value as kotlin.String)
                }
            }
            else -> {
                throw IllegalArgumentException("ResourceRequestPatcher not applicable for $k8sResource")
            }
        }
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
