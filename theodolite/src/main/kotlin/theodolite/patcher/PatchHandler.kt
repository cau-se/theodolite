package theodolite.patcher

import io.fabric8.kubernetes.api.model.HasMetadata
import theodolite.util.InvalidPatcherConfigurationException
import theodolite.util.PatcherDefinition

class PatchHandler {

    companion object {

        private fun getResourcesToPatch(resources: MutableMap<String, List<HasMetadata>>, patcherDefinition: PatcherDefinition): List<HasMetadata> {
            return resources[patcherDefinition.resource]
                ?: throw InvalidPatcherConfigurationException("Could not find resource ${patcherDefinition.resource}")

        }

        fun patchResource(
            resources: MutableMap<String, List<HasMetadata>>,
            patcherDefinition: PatcherDefinition,
            value: String,
        ): List<HasMetadata> {
            val resToPatch = getResourcesToPatch(resources, patcherDefinition)
            return PatcherFactory.createPatcher(patcherDefinition).patch(resToPatch,value)
        }
    }
}