package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class PatcherDefinition {
    lateinit var type: String
    lateinit var resource: String
    lateinit var container: String
    lateinit var variableName: String
}
