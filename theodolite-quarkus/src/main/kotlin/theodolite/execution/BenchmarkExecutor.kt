package theodolite.execution

import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

abstract class BenchmarkExecutor(val benchmark: Benchmark, val results: Results, val executionDuration: Duration) {
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean;
}