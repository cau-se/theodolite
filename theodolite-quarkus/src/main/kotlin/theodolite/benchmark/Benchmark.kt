package theodolite.benchmark

import theodolite.util.LoadDimension
import theodolite.util.OverridePatcherDefinition
import theodolite.util.Resource

interface Benchmark {
    fun buildDeployment(load: LoadDimension, res: Resource, overrides: List<OverridePatcherDefinition>): BenchmarkDeployment
}