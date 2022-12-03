package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Abstract [Patcher] to set resource limits or requests to Deployments and StatefulSets.
 *
 * @param container Container to be patched.
 * @param requiredResource The resource to be requested or limited (e.g., **cpu** or **memory**)
 * @param format Format add to the provided value (e.g., `GBi` or `m`, see [Quantity]).
 * @param factor A factor to multiply the provided value with.
 */
abstract class AbstractResourcePatcher(
    private val container: String,
    protected val requiredResource: String,
    private val format: String? = null,
    private val factor: Int? = null
) : AbstractStringPatcher() {

    final override fun patchSingleResource(resource: HasMetadata, value: String): HasMetadata {
        when (resource) {
            is Deployment -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setValues(it, value)
                }
            }
            is StatefulSet -> {
                resource.spec.template.spec.containers.filter { it.name == container }.forEach {
                    setValues(it, value)
                }
            }
            else -> {
                throw InvalidPatcherConfigurationException("ResourceLimitPatcher is not applicable for $resource.")
            }
        }
        return resource
    }

    private fun setValues(container: Container, value: String) {
        val quantity = if (this.format != null || this.factor != null) {
            val amountAsInt = value.toIntOrNull()?.times(this.factor ?: 1)
            if (amountAsInt == null) {
                logger.warn { "Patcher value cannot be parsed as Int. Ignoring quantity format and factor." }
                Quantity(value)
            } else {
                Quantity(amountAsInt.toString(), format ?: "")
            }
        } else {
            Quantity(value)
        }

        setValues(container, quantity)
    }

    abstract fun setValues(container: Container, quantity: Quantity)
}