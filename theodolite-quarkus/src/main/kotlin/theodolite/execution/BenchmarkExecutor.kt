package theodolite.execution

import theodolite.util.Benchmark
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results
import java.time.Duration

/**
 * The Benchmark Executor runs a single experiment.
 *
 * @property benchmark
 * @property results
 * @property executionDuration
 * @constructor Create empty Benchmark executor
 */
abstract class BenchmarkExecutor(val benchmark: Benchmark, val results: Results, val executionDuration: Duration) {
    /**
     * Run a experiment for the given parametrization, evaluate the experiment and save the result.
     *
     * @param load load to be tested.
     * @param res resources to be tested.
     * @return True, if the number of resources are suitable for the given load, false otherwise.
     */
    abstract fun runExperiment(load: LoadDimension, res: Resource): Boolean;
}