package theodolite.benchmark

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource

/**
 * A Benchmark contains:
 * - The [Resource]s that can be scaled for the benchmark.
 * - The [LoadDimension]s that can be scaled the benchmark.
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
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride?>,
        loadGenerationDelay: Long,
        afterTeardownDelay: Long
    ): BenchmarkDeployment
}
