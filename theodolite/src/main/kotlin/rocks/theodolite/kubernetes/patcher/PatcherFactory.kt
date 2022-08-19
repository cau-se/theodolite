package rocks.theodolite.kubernetes.patcher

/**
 * The Patcher factory creates [Patcher]s
 *
 * @constructor Creates an empty PatcherFactory.
 */
class PatcherFactory {

    companion object {
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
        ): Patcher {

            return try {
                when (patcherDefinition.type) {
                    "ReplicaPatcher" -> ReplicaPatcher(
                    )
                    "NumNestedGroupsLoadGeneratorReplicaPatcher" -> NumNestedGroupsLoadGeneratorReplicaPatcher(
                        loadGenMaxRecords = patcherDefinition.properties["loadGenMaxRecords"]!!,
                        numSensors = patcherDefinition.properties["numSensors"]!!
                    )
                    "NumSensorsLoadGeneratorReplicaPatcher" -> NumSensorsLoadGeneratorReplicaPatcher(
                        loadGenMaxRecords = patcherDefinition.properties["loadGenMaxRecords"]!!
                    )
                    "DataVolumeLoadGeneratorReplicaPatcher" -> DataVolumeLoadGeneratorReplicaPatcher(
                        maxVolume = patcherDefinition.properties["maxVolume"]!!.toInt(),
                        container = patcherDefinition.properties["container"]!!,
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "EnvVarPatcher" -> EnvVarPatcher(
                        container = patcherDefinition.properties["container"]!!,
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "NodeSelectorPatcher" -> NodeSelectorPatcher(
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "ResourceLimitPatcher" -> ResourceLimitPatcher(
                        container = patcherDefinition.properties["container"]!!,
                        limitedResource = patcherDefinition.properties["limitedResource"]!!,
                        format = patcherDefinition.properties["format"],
                        factor = patcherDefinition.properties["factor"]?.toInt()
                    )
                    "ResourceRequestPatcher" -> ResourceRequestPatcher(
                        container = patcherDefinition.properties["container"]!!,
                        requestedResource = patcherDefinition.properties["requestedResource"]!!,
                        format = patcherDefinition.properties["format"],
                        factor = patcherDefinition.properties["factor"]?.toInt()
                    )
                    "SchedulerNamePatcher" -> SchedulerNamePatcher()
                    "LabelPatcher" -> LabelPatcher(
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "MatchLabelPatcher" -> MatchLabelPatcher(
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "TemplateLabelPatcher" -> TemplateLabelPatcher(
                        variableName = patcherDefinition.properties["variableName"]!!
                    )
                    "ImagePatcher" -> ImagePatcher(
                        container = patcherDefinition.properties["container"]!!
                    )
                    "NamePatcher" -> NamePatcher()
                    "ServiceSelectorPatcher" -> ServiceSelectorPatcher(
                        variableName = patcherDefinition.properties["label"]!!
                    )
                    "VolumesConfigMapPatcher" -> VolumesConfigMapPatcher(
                        volumeName = patcherDefinition.properties["volumeName"]!!
                    )
                    else -> throw InvalidPatcherConfigurationException("Patcher type ${patcherDefinition.type} not found.")
                }
            } catch (e: NullPointerException) {
                throw InvalidPatcherConfigurationException(
                    "Could not create patcher with type ${patcherDefinition.type}" +
                            " Probably a required patcher argument was not specified.", e
                )
            }
        }
    }
}
