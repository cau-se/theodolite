package theodolite

import rocks.theodolite.kubernetes.benchmark.Benchmark
import rocks.theodolite.kubernetes.benchmark.BenchmarkDeployment
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.util.PatcherDefinition

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
