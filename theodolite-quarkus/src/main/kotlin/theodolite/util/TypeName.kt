package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class TypeName {
    lateinit var typeName: String
    lateinit var patchers: List<PatcherDefinition>
}
