package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

/**
 *  Linear-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class LinearSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        for (res in resources) {
            if (this.benchmarkExecutor.runExperiment(load, res)) return res
        }
        return null
    }
}
