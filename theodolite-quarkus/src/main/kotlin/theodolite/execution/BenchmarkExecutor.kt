package theodolite.execution

import theodolite.util.LoadDimension
import theodolite.util.Resource

interface BenchmarkExecutor {
    fun runExperiment(load: LoadDimension, res: Resource): Boolean;
}