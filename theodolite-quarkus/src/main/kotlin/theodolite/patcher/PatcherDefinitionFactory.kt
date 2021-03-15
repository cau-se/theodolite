package theodolite.patcher

import theodolite.util.PatcherDefinition
import theodolite.util.TypeName

class PatcherDefinitionFactory {
    fun createPatcherDefinition(requiredType: String, patcherTypes: List<TypeName>) : List<PatcherDefinition> {
        return patcherTypes
            .filter { type -> type.typeName == requiredType }
            .flatMap { type -> type.patchers }
    }

    fun getEmptyPatcherDefinition(): PatcherDefinition {
        val emptyDef =  PatcherDefinition()
        emptyDef.type = ""
        emptyDef.resource = ""
        emptyDef.container = ""
        emptyDef.variableName = ""
        return emptyDef
    }
}