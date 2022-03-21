package rocks.theodolite.kubernetes


import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.patcher.PatcherDefinition

class TestBenchmarkDeploymentBuilder(): BenchmarkDeploymentBuilder {

    override fun buildDeployment(
            load: Int,
            loadPatcherDefinitions: List<PatcherDefinition>,
            resource: Int,
            resourcePatcherDefinitions: List<PatcherDefinition>,
            configurationOverrides: List<ConfigurationOverride?>,
            loadGenerationDelay: Long,
            afterTeardownDelay: Long
    ): BenchmarkDeployment {
        return TestBenchmarkDeployment()
    }
}
