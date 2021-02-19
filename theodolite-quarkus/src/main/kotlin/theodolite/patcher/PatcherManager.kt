package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
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

    private fun getPatcherDef(requiredType: String, resourceTypes: List<TypeName>): List<PatcherDefinition> {
        return resourceTypes
            .filter { type -> type.typeName == requiredType}
            .flatMap { type -> type.patchers}
    }

    fun applyPatcher(type: String, resourceTypes: List<TypeName>, resources: List<Pair<String, KubernetesResource>>, value: Any) {
        this.getPatcherDef(type, resourceTypes)
            .forEach {patcherDef ->
                createK8sPatcher(patcherDef, resources).patch(value) }

    }

}