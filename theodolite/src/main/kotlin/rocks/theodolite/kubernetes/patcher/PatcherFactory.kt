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
         * @param patcher The [PatcherDefinition] for which are [Patcher] should be created.
         * @return The created [Patcher].
         * @throws IllegalArgumentException if no patcher can be created.
         */
        fun createPatcher(patcher: PatcherDefinition): Patcher {
            return when (patcher.type) {
                    "ReplicaPatcher" -> ReplicaPatcher(
                    )
                    "NumNestedGroupsLoadGeneratorReplicaPatcher" -> NumNestedGroupsLoadGeneratorReplicaPatcher(
                        loadGenMaxRecords = patcher.properties["loadGenMaxRecords"] ?: throwInvalid(patcher),
                        numSensors = patcher.properties["numSensors"] ?: throwInvalid(patcher)
                    )
                    "NumSensorsLoadGeneratorReplicaPatcher" -> NumSensorsLoadGeneratorReplicaPatcher(
                        loadGenMaxRecords = patcher.properties["loadGenMaxRecords"] ?: throwInvalid(patcher)
                    )
                    "DataVolumeLoadGeneratorReplicaPatcher" -> DataVolumeLoadGeneratorReplicaPatcher(
                        maxVolume = (patcher.properties["maxVolume"] ?: throwInvalid(patcher)).toInt(),
                        container = patcher.properties["container"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "EnvVarPatcher" -> EnvVarPatcher(
                        container = patcher.properties["container"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "NodeSelectorPatcher" -> NodeSelectorPatcher(
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "ResourceLimitPatcher" -> ResourceLimitPatcher(
                        container = patcher.properties["container"] ?: throwInvalid(patcher),
                        limitedResource = patcher.properties["limitedResource"] ?: throwInvalid(patcher),
                        format = patcher.properties["format"],
                        factor = patcher.properties["factor"]?.toInt()
                    )
                    "ResourceRequestPatcher" -> ResourceRequestPatcher(
                        container = patcher.properties["container"] ?: throwInvalid(patcher),
                        requestedResource = patcher.properties["requestedResource"] ?: throwInvalid(patcher),
                        format = patcher.properties["format"],
                        factor = patcher.properties["factor"]?.toInt()
                    )
                    "SchedulerNamePatcher" -> SchedulerNamePatcher()
                    "LabelPatcher" -> LabelPatcher(
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "MatchLabelPatcher" -> MatchLabelPatcher(
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "TemplateLabelPatcher" -> TemplateLabelPatcher(
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "ImagePatcher" -> ImagePatcher(
                        container = patcher.properties["container"] ?: throwInvalid(patcher)
                    )
                    "ConfigMapYamlPatcher" -> ConfigMapYamlPatcher(
                        fileName = patcher.properties["fileName"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    )
                    "NamePatcher" -> NamePatcher()
                    "ServiceSelectorPatcher" -> ServiceSelectorPatcher(
                        variableName = patcher.properties["label"] ?: throwInvalid(patcher)
                    )
                    "VolumesConfigMapPatcher" -> VolumesConfigMapPatcher(
                        volumeName = patcher.properties["volumeName"] ?: throwInvalid(patcher)
                    )
                    else -> throw InvalidPatcherConfigurationException("Patcher type ${patcher.type} not found.")
                }
        }

        private fun throwInvalid(patcher: PatcherDefinition): String {
            throw InvalidPatcherConfigurationException("Could not create patcher with type ${patcher.type}. Probably a required patcher argument was not specified.")
        }
    }

}
