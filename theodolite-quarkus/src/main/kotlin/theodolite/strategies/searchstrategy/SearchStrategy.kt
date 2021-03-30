package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

/**
 *  Base class for the implementation for SearchStrategies. SearchStrategies determine the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
@RegisterForReflection
abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor) {
    /**
     * Find smallest suitable resource from the specified resource list for the given load.
     *
     * @param load Load to be tested.
     * @param resources List of all possible resources.
     *
     * @return suitable resource for the specified load, or null if no suitable resource exists.
     *
     * @see LoadDimension
     * @see Resource
     */
    abstract fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource?
}
