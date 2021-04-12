package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

@JsonDeserialize
@RegisterForReflection
class PatcherDefinition {
    lateinit var type: String
    lateinit var resource: String
    lateinit var container: String
    lateinit var variableName: String
}
