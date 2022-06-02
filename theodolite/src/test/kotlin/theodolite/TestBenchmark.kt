package theodolite

import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkDeployment
import theodolite.util.ConfigurationOverride
import theodolite.util.PatcherDefinition

class TestBenchmark : Benchmark {

    override fun setupInfrastructure() {
    }

    override fun teardownInfrastructure() {
    }

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
