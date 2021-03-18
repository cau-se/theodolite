package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
class PatcherDefinition {
    lateinit var type: String
    lateinit var resource: String
    lateinit var container: String
    lateinit var variableName: String
}
