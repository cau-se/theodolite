package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
class ConfigurationOverride {
    lateinit var patcher: PatcherDefinition
    lateinit var value: String
}
