package theodolite.execution

import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

abstract class BenchmarkExecutor(val benchmark: Benchmark, val results: Results) {
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean;
}