package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.ResourceRequirements
import io.fabric8.kubernetes.api.model.apps.Deployment

class ResourceLimitPatcher(
    private val k8sResource: KubernetesResource,
    private val container: String,
    private val variableName: String
) : AbstractPatcher(k8sResource, container, variableName) {

    override fun <String> patch(value: String) {

        if (k8sResource is Deployment) {
            k8sResource.spec.template.spec.containers.filter { it.name == container }.forEach {
                try {
                    println("before: " + it.resources.limits.toString())
                    println("$variableName to : $value")
                    if (it.resources.limits.isNullOrEmpty()) {
                        it.resources.limits = mapOf(variableName to Quantity(value as kotlin.String))
                    } else {
                        val values = Quantity(value as kotlin.String)
                        it.resources.limits[variableName] = values
                    }
                } catch (e: IllegalStateException) {
                    val resource = ResourceRequirements()
                    resource.limits = mapOf(variableName to Quantity(value as kotlin.String))
                    it.resources = resource
                }

                println("after " + it.resources.limits.toString())

            }
        }
    }
}
