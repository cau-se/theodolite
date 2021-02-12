package theodolite.benchmark

import theodolite.util.LoadDimension
import theodolite.util.Resource

class KubernetesBenchmark(): Benchmark {
    lateinit var name: String

    override fun buildDeployment(load: LoadDimension, res: Resource): BenchmarkDeployment {
        TODO("Not yet implemented")
    }
}