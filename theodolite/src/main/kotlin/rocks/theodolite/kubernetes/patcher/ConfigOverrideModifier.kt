package rocks.theodolite.kubernetes.patcher

import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.util.ConfigurationOverride

/**
 * The ConfigOverrideModifier makes it possible to update the configuration overrides of an execution.
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
        val additionalConfigOverrides = resources.map {
            ConfigurationOverride().apply {
                this.patcher = PatcherDefinition()
                this.patcher.type = "LabelPatcher"
                this.patcher.properties = mapOf("variableName" to labelName)
                this.patcher.resource = it
                this.value = labelValue
            }
        }
        execution.configOverrides.addAll(additionalConfigOverrides)
    }
}