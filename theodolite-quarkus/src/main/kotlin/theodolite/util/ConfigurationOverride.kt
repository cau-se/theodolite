package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
class ConfigurationOverride {
    lateinit var patcher: PatcherDefinition
    lateinit var value: String
}
