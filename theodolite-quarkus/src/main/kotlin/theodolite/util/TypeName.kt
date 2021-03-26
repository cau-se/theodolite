package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
@JsonDeserialize
class TypeName {
    lateinit var typeName: String
    lateinit var patchers: List<PatcherDefinition>
}
