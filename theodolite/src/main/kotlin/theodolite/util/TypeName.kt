package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * The TypeName encapsulates a list of [PatcherDefinition] along with a typeName that specifies for what the [PatcherDefinition] should be used.
 */
@RegisterForReflection
@JsonDeserialize
class TypeName {
    lateinit var typeName: String
    lateinit var patchers: List<PatcherDefinition>
}
