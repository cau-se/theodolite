package theodolite.benchmark

import theodolite.util.LoadDimension
import theodolite.util.ConfigurationOverride
import theodolite.util.Resource

class TestBenchmark : Benchmark {

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride>
    ): BenchmarkDeployment {
        return TestBenchmarkDeployment()
    }
}
