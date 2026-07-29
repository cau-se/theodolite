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
                    loadGenMaxRecords = (patcher.properties["loadGenMaxRecords"] ?: throwInvalid(patcher)).toInt(),
                    numSensors = (patcher.properties["numSensors"] ?: throwInvalid(patcher)).toInt()
                )
                "NumSensorsLoadGeneratorReplicaPatcher" -> NumSensorsLoadGeneratorReplicaPatcher(
                    loadGenMaxRecords = (patcher.properties["loadGenMaxRecords"] ?: throwInvalid(patcher)).toInt()
                )
                "DataVolumeLoadGeneratorReplicaPatcher" -> DataVolumeLoadGeneratorReplicaPatcher(
                    maxVolume = (patcher.properties["maxVolume"] ?: throwInvalid(patcher)).toInt(),
                    container = patcher.properties["container"] ?: throwInvalid(patcher),
                    variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                )
                "EnvVarPatcher" -> DecoratingPatcher(
                    EnvVarPatcher(
                        container = patcher.properties["container"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher),
                        isInitContainer = patcher.properties["isInitContainer"]?.toBoolean() ?: false,
                    ),
                    prefix = patcher.properties["prefix"],
                    suffix = patcher.properties["suffix"],
                    factor = patcher.properties["factor"]?.toInt(),
                )
                "NodeSelectorPatcher" -> NodeSelectorPatcher(
                        nodeLabelName = patcher.properties["nodeLabelName"]
                                ?: patcher.properties["variableName"] // TODO Deprecate!
                                ?: throwInvalid(patcher)
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
                    labelName = patcher.properties["labelName"]
                            ?: patcher.properties["variableName"] // TODO Deprecate!
                            ?: throwInvalid(patcher)
                )
                "MatchLabelPatcher" -> MatchLabelPatcher(
                    labelName = patcher.properties["labelName"]
                            ?: patcher.properties["variableName"] // TODO Deprecate!
                            ?: throwInvalid(patcher)
                )
                "TemplateLabelPatcher" -> TemplateLabelPatcher(
                    labelName = patcher.properties["labelName"]
                            ?: patcher.properties["variableName"] // TODO Deprecate!
                            ?: throwInvalid(patcher)
                )
                "ImagePatcher" -> ImagePatcher(
                    container = patcher.properties["container"] ?: throwInvalid(patcher),
                    pullPolicy = patcher.properties["pullPolicy"]
                )
                "ConfigMapYamlPatcher" -> DecoratingPatcher(
                    ConfigMapYamlPatcher(
                        fileName = patcher.properties["fileName"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    ),
                    prefix = patcher.properties["prefix"],
                    suffix = patcher.properties["suffix"],
                    factor = patcher.properties["factor"]?.toInt(),
                )
                "ConfigMapPropertiesPatcher" -> DecoratingPatcher(
                    ConfigMapPropertiesPatcher(
                        fileName = patcher.properties["fileName"] ?: throwInvalid(patcher),
                        variableName = patcher.properties["variableName"] ?: throwInvalid(patcher)
                    ),
                    prefix = patcher.properties["prefix"],
                    suffix = patcher.properties["suffix"],
                    factor = patcher.properties["factor"]?.toInt(),
                )
                "NamePatcher" -> NamePatcher()
                "ServiceSelectorPatcher" -> ServiceSelectorPatcher(
                    labelName = patcher.properties["labelName"]
                            ?: patcher.properties["label"] // TODO Deprecate!
                            ?: throwInvalid(patcher)
                )
                "VolumesConfigMapPatcher" -> VolumesConfigMapPatcher(
                    volumeName = patcher.properties["volumeName"] ?: throwInvalid(patcher)
                )
                "GenericResourcePatcher" -> DecoratingPatcher(
                    GenericResourcePatcher(
                        path = (patcher.properties["path"] ?: throwInvalid(patcher))
                                .removePrefix("/")
                                .split("/")
                                .map { it.toIntOrNull() ?: it },
                        type = patcher.properties["type"]?.let { GenericResourcePatcher.Type.from(it) } ?: GenericResourcePatcher.Type.STRING
                    ),
                    prefix = patcher.properties["prefix"],
                    suffix = patcher.properties["suffix"],
                    factor = patcher.properties["factor"]?.toInt()
                )
                else -> throw InvalidPatcherConfigurationException("Patcher type ${patcher.type} not found.")
            }
        }

        private fun throwInvalid(patcher: PatcherDefinition): String {
            throw InvalidPatcherConfigurationException("Could not create patcher with type ${patcher.type}. Probably a required patcher argument was not specified.")
        }
    }

}
