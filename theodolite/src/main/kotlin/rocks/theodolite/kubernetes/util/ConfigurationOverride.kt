package rocks.theodolite.kubernetes.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.patcher.PatcherDefinition

/**
 * Representation of a configuration override.
 */
@JsonDeserialize
@RegisterForReflection
class ConfigurationOverride {
    /**
     * Patcher of the configuration override.
     */
    lateinit var patcher: PatcherDefinition

    /**
     * Value of the patched configuration override.
     */
    lateinit var value: String
}
