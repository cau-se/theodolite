package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * A decorating [Patcher] to modifies a value by a factor, a prefix and a suffix. All modifications are optional. The
 * factor only has an effect if the supplied value is an integer.
 */
class DecoratingPatcher(
    val innerPatcher: Patcher,
    private val prefix: String?,
    private val suffix: String?,
    private val factor: Int?
) : Patcher {

    override fun patch(resources: List<HasMetadata>, value: String) : List<HasMetadata> {
        return this.innerPatcher.patch(resources, (prefix ?: "") + multiply(value) + (suffix ?: ""))
    }

    private fun multiply(value: String): String {
        if (this.factor == null) {
            return value
        }
        val valueAsInt = value.toIntOrNull()
        if (valueAsInt == null) {
            logger.warn { "Patcher value cannot be parsed as Int. Ignoring factor." }
            return value
        }
        return valueAsInt.times(factor).toString()
    }

}
