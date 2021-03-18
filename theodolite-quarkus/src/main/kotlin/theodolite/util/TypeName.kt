package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
class TypeName {
    lateinit var typeName: String
    lateinit var patchers: List<PatcherDefinition>
}
