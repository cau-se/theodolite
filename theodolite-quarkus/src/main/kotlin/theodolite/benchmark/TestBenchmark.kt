package theodolite.benchmark

import theodolite.util.LoadDimension
import theodolite.util.ConfigurationOverride
import theodolite.util.Resource

class TestBenchmark : Benchmark {

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverride: List<ConfigurationOverride>
    ): BenchmarkDeployment {
        return TestBenchmarkDeployment()
    }
}
