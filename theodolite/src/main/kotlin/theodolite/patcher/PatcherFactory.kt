package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource
import theodolite.util.DeploymentFailedException
import theodolite.util.InvalidPatcherConfigurationException
import theodolite.util.PatcherDefinition

/**
 * The Patcher factory creates [Patcher]s
 *
 * @constructor Creates an empty PatcherFactory.
 */
class PatcherFactory {
    /**
     * Create patcher based on the given [PatcherDefinition] and
     * the list of KubernetesResources.
     *
     * @param patcherDefinition The [PatcherDefinition] for which are
     *     [Patcher] should be created.
     * @param k8sResources List of all available Kubernetes resources.
     *     This is a list of pairs<String, KubernetesResource>:
     *     The frist corresponds to the filename where the resource is defined.
     *     The second corresponds to the concrete [KubernetesResource] that should be patched.
     * @return The created [Patcher].
     * @throws IllegalArgumentException if no patcher can be created.
     */
    fun createPatcher(
        patcherDefinition: PatcherDefinition,
        k8sResources: List<Pair<String, KubernetesResource>>
    ): Patcher {
        val resource =
            k8sResources.filter { it.first == patcherDefinition.resource }
                .map { resource -> resource.second }
                .firstOrNull()
                ?: throw DeploymentFailedException("Could not find resource ${patcherDefinition.resource}")

        return try {
            when (patcherDefinition.type) {
                "ReplicaPatcher" -> ReplicaPatcher(
                    k8sResource = resource
                )
                "NumNestedGroupsLoadGeneratorReplicaPatcher" -> NumNestedGroupsLoadGeneratorReplicaPatcher(
                    k8sResource = resource,
                    loadGenMaxRecords = patcherDefinition.properties["loadGenMaxRecords"] !!,
                    numSensors = patcherDefinition.properties["numSensors"] !!
                )
                "NumSensorsLoadGeneratorReplicaPatcher" -> NumSensorsLoadGeneratorReplicaPatcher(
                    k8sResource = resource,
                    loadGenMaxRecords = patcherDefinition.properties["loadGenMaxRecords"] !!
                )
                "EnvVarPatcher" -> EnvVarPatcher(
                    k8sResource = resource,
                    container = patcherDefinition.properties["container"] !!,
                    variableName = patcherDefinition.properties["variableName"] !!
                )
                "NodeSelectorPatcher" -> NodeSelectorPatcher(
                    k8sResource = resource,
                    variableName = patcherDefinition.properties["variableName"] !!
                )
                "ResourceLimitPatcher" -> ResourceLimitPatcher(
                    k8sResource = resource,
                    container = patcherDefinition.properties["container"] !!,
                    limitedResource = patcherDefinition.properties["limitedResource"] !!
                )
                "ResourceRequestPatcher" -> ResourceRequestPatcher(
                    k8sResource = resource,
                    container = patcherDefinition.properties["container"] !!,
                    requestedResource = patcherDefinition.properties["requestedResource"] !!
                )
                "SchedulerNamePatcher" -> SchedulerNamePatcher(
                    k8sResource = resource
                )
                "LabelPatcher" -> LabelPatcher(
                    k8sResource = resource,
                    variableName = patcherDefinition.properties["variableName"] !!
                )
                else -> throw InvalidPatcherConfigurationException("Patcher type ${patcherDefinition.type} not found.")
            }
        } catch (e: Exception) {
            throw InvalidPatcherConfigurationException("Could not create patcher with type ${patcherDefinition.type}" +
                    " Probably a required patcher argument was not specified." )
        }
    }
}
