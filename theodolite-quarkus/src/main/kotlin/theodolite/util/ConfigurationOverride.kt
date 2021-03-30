package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Representation of a configuration override.
 *
 * @param patcher the PatcherDefinition
 * @param value the value of the configuration override
 *
 * @see PatcherDefinition
 */
@RegisterForReflection
class ConfigurationOverride {
    lateinit var patcher: PatcherDefinition
    lateinit var value: String
}
