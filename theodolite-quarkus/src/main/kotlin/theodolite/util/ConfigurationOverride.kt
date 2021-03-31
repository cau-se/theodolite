package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of a configuration override.
 *
 * @param value the value of the configuration override
 *
 * @see PatcherDefinition
 */
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
