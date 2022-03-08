package rocks.theodolite.kubernetes.benchmark

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.util.PatcherDefinition

/**
 * A Benchmark contains:
 * - The Resource that can be scaled for the benchmark.
 * - The Load that can be scaled the benchmark.
 * - additional [ConfigurationOverride]s.
 */
@RegisterForReflection
interface Benchmark {

    fun setupInfrastructure()
    fun teardownInfrastructure()

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
