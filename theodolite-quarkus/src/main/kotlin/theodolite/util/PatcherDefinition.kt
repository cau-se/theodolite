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
    lateinit var properties: MutableMap<String, String>
}
