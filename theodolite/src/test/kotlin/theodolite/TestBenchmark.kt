package theodolite

import theodolite.benchmark.Benchmark
import theodolite.benchmark.BenchmarkDeployment
import theodolite.util.ConfigurationOverride
import theodolite.util.LoadDimension
import theodolite.util.Resources

class TestBenchmark : Benchmark {

    override fun setupInfrastructure() {
    }

    override fun teardownInfrastructure() {
    }

    override fun buildDeployment(
            load: LoadDimension,
            res: Resources,
            configurationOverrides: List<ConfigurationOverride?>,
            loadGenerationDelay: Long,
            afterTeardownDelay: Long
    ): BenchmarkDeployment {
        return TestBenchmarkDeployment()
    }
}
