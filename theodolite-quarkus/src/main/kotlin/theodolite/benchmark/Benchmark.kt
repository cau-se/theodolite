package theodolite.benchmark

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource

/**
 * A Benchmark contains:
 * - The resources to be benchmarked.
 * - The [Resource]s that can be scaled for the benchmark.
 * - The [LoadDimension]s that can be scaled the benchmark.
 * - additional infrastructure configurations.
 */
@RegisterForReflection
interface Benchmark {

    /**
     * Builds a Deployment that can be deployed.
     * @return a BenchmarkDeployment.
     */
    fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride?>
    ): BenchmarkDeployment
}
