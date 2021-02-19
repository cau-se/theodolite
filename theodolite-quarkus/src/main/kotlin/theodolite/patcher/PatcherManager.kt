package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import theodolite.util.OverridePatcherDefinition
import theodolite.util.PatcherDefinition
import theodolite.util.TypeName
import java.lang.IllegalArgumentException

class PatcherManager {
    private fun createK8sPatcher(patcherDefinition: PatcherDefinition, k8sResources: List<Pair<String, KubernetesResource>>): Patcher {
        return when(patcherDefinition.type) {
            "ReplicaPatcher" -> ReplicaPatcher(k8sResources.filter { it.first == patcherDefinition.resource}.map { resource -> resource.second }[0])
            "EnvVarPatcher" -> EnvVarPatcher(k8sResources.filter { it.first == patcherDefinition.resource}.map { resource -> resource.second }[0],
                                patcherDefinition.container,
                                patcherDefinition.variableName)
            else -> throw IllegalArgumentException("Patcher type ${patcherDefinition.type} not found")
        }
    }

    private fun getPatcherDef(requiredType: String, patcherTypes: List<TypeName>): List<PatcherDefinition> {
        return patcherTypes
            .filter { type -> type.typeName == requiredType}
            .flatMap { type -> type.patchers}
    }

    fun applyPatcher(type: String, patcherTypes: List<TypeName>, resources: List<Pair<String, KubernetesResource>>, value: Any) {
        this.getPatcherDef(type, patcherTypes)
            .forEach {patcherDef ->
                createK8sPatcher(patcherDef, resources).patch(value) }
    }


   fun applyPatcher(overrides: OverridePatcherDefinition, resources: List<Pair<String, KubernetesResource>>){
       var pdef = PatcherDefinition()
       pdef.type = overrides.type
       pdef.container = overrides.container
       pdef.resource = overrides.resource
       overrides.overrides.forEach{ override ->
           pdef.variableName = override.key
           this.createK8sPatcher(pdef, resources).patch(override.value)
       }
   }

}