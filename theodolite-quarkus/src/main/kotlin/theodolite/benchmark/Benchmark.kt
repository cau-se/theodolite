package theodolite.benchmark

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resource

@RegisterForReflection
interface Benchmark {
    fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride>
    ): BenchmarkDeployment
}
