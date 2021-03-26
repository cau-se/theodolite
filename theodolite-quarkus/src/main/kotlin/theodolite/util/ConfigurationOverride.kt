package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class ConfigurationOverride {
    lateinit var patcher: PatcherDefinition
    lateinit var value: String
}
