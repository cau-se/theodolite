package theodolite.benchmark

import theodolite.util.LoadDimension
import theodolite.util.OverridePatcherDefinition
import theodolite.util.Resource

class TestBenchmark : Benchmark {

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        override: List<OverridePatcherDefinition>
    ): BenchmarkDeployment {
        return TestBenchmarkDeployment()
    }
}
