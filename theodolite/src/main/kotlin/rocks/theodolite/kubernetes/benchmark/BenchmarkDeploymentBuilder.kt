package rocks.theodolite.kubernetes.benchmark

import rocks.theodolite.kubernetes.patcher.PatcherDefinition
import rocks.theodolite.kubernetes.util.ConfigurationOverride

/**
 * This interface is needed for test purposes.
 */
interface BenchmarkDeploymentBuilder {

    /**
     * Builds a Deployment that can be deployed.
     * @return a BenchmarkDeployment.
     */
    fun buildDeployment(
            load: Int,
            loadPatcherDefinitions: List<PatcherDefinition>,
            resource: Int,
            resourcePatcherDefinitions: List<PatcherDefinition>,
            configurationOverrides: List<ConfigurationOverride?>,
            loadGenerationDelay: Long,
            afterTeardownDelay: Long
    ): BenchmarkDeployment
}