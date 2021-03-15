package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import theodolite.util.PatcherDefinition
import theodolite.util.TypeName

class PatcherManager {
    private fun createK8sPatcher(
        patcherDefinition: PatcherDefinition,
        k8sResources: List<Pair<String, KubernetesResource>>
    ): Patcher {
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
            "SchedulerNamePatcher" -> SchedulerNamePatcher(resource)
            else -> throw IllegalArgumentException("Patcher type ${patcherDefinition.type} not found")
        }
    }

    private fun getPatcherDef(requiredType: String, patcherTypes: List<TypeName>): List<PatcherDefinition> {
        return patcherTypes
            .filter { type -> type.typeName == requiredType }
            .flatMap { type -> type.patchers }
    }

    /**
     * This function first creates a patcher definition and
     * then patches the list of resources based on this patcher definition
     *
     * @param type Patcher type, for example "EnvVarPatcher"
     * @param patcherTypes List of patcher types definitions, for example for resources and threads
     * @param resources List of K8s resources, a patcher takes the resources that are needed
     * @param value The value to patch
     */
    fun createAndApplyPatcher(
        type: String,
        patcherTypes: List<TypeName>,
        resources: List<Pair<String, KubernetesResource>>,
        value: Any
    ) {
        this.getPatcherDef(type, patcherTypes)
            .forEach { patcherDef ->
                createK8sPatcher(patcherDef, resources).patch(value)
            }
    }

    /**
     * Patch a resource based on the given patcher definition, a list of resources and a value to patch
     *
     * @param patcherDefinition The patcher definition
     * @param resources List of patcher types definitions, for example for resources and threads
     * @param value The value to patch
     */
    fun applyPatcher(
        patcherDefinition: List<PatcherDefinition>,
        resources: List<Pair<String, KubernetesResource>>,
        value: Any
    ) {
        patcherDefinition.forEach { def -> this.createK8sPatcher(def, resources).patch(value) }
    }
}
