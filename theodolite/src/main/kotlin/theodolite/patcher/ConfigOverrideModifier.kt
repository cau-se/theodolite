package theodolite.patcher

import theodolite.benchmark.BenchmarkExecution
import theodolite.util.ConfigurationOverride
import theodolite.util.PatcherDefinition

/**
 * The ConfigOverrideModifier makes it possible to update the configuration overrides of a execution.
 *
 * @property execution execution for which the config overrides should be updated
 * @property resources list of all resources that should be updated.
 */
class ConfigOverrideModifier(val execution: BenchmarkExecution, val resources: List<String>) {

    /**
     * Adds a [LabelPatcher] to the configOverrides.
     *
     * @param labelValue value argument for the label patcher
     * @param labelName  label name argument for the label patcher
     */
    fun setAdditionalLabels(
        labelValue: String,
        labelName: String
    ) {
        val additionalConfigOverrides = mutableListOf<ConfigurationOverride>()
        resources.forEach {
            run {
                val configurationOverride = ConfigurationOverride()
                configurationOverride.patcher = PatcherDefinition()
                configurationOverride.patcher.type = "LabelPatcher"
                configurationOverride.patcher.properties = mutableMapOf("variableName" to labelName)
                configurationOverride.patcher.resource = it
                configurationOverride.value = labelValue
                additionalConfigOverrides.add(configurationOverride)
            }
        }
        execution.configOverrides.addAll(additionalConfigOverrides)
    }
}