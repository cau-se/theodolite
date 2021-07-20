package theodolite.patcher

import theodolite.util.PatcherDefinition
import theodolite.util.TypeName

/**
 * The PatcherDefinition Factory creates a [PatcherDefinition]s.
 *
 * @constructor Create empty Patcher definition factory.
 */
class PatcherDefinitionFactory {
    /**
     * Creates a list of PatcherDefinitions
     *
     * @param requiredType indicates the required PatcherDefinitions
     *     (for example `NumSensors`)
     * @param patcherTypes list of TypeNames. A TypeName contains a type
     *     (for example `NumSensors`) and a list of
     *     PatcherDefinitions, which are related to this type.
     * @return A list of PatcherDefinitions which corresponds to the
     *     value of the requiredType.
     */
    fun createPatcherDefinition(requiredType: String, patcherTypes: List<TypeName>): List<PatcherDefinition> {
        return patcherTypes.firstOrNull() { type -> type.typeName == requiredType }
            ?.patchers ?: throw IllegalArgumentException("typeName $requiredType not found.")
    }
}
