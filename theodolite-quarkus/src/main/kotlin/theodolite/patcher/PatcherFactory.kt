package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import theodolite.util.PatcherDefinition

class PatcherFactory {
    fun createPatcher(patcherDefinition: PatcherDefinition,
                      k8sResources: List<Pair<String, KubernetesResource>>) : Patcher {
        val resource =
            k8sResources.filter { it.first == patcherDefinition.resource }.map { resource -> resource.second }[0]
        return when (patcherDefinition.type) {
            "ReplicaPatcher" -> ReplicaPatcher(resource)
            "EnvVarPatcher" -> EnvVarPatcher(resource, patcherDefinition.container, patcherDefinition.variableName)
            "NodeSelectorPatcher" -> NodeSelectorPatcher(resource, patcherDefinition.variableName)
            "ResourceLimitPatcher" -> ResourceLimitPatcher(
                resource,
                patcherDefinition.container,
                patcherDefinition.variableName
            )
            "ResourceRequestPatcher" -> ResourceRequestPatcher(
                resource,
                patcherDefinition.container,
                patcherDefinition.variableName
            )
            else -> throw IllegalArgumentException("Patcher type ${patcherDefinition.type} not found")
        }
    }
}