package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Definition of the structure of a [theodolite.patcher.AbstractPatcher] which implements the [theodolite.patcher.Patcher] interface.
 */
@JsonDeserialize
@RegisterForReflection
class PatcherDefinition {
    /**
     * The type of the patcher
     */
    lateinit var type: String

    /**
     * The resource which the patcher is applied to
     */
    lateinit var resource: String

    @JsonSerialize
    lateinit var config: MutableList<Map<String, String>>

    fun getValueByKey(key: String): String {
        val value = this.config
            .filter { it["key"] == key }
            .map {
                try {
                    it.getValue("value")
                } catch (e: Exception) {
                    throw InvalidPatcherConfigurationException("No value found for key $key.")
                }
            }

        return when {
            value.isEmpty() -> throw InvalidPatcherConfigurationException("Required argument $key missing.")
            value.size > 1 -> throw InvalidPatcherConfigurationException("Can not handle duplicate declaration for key $key.")
            else -> value.first()
        }
    }
}
