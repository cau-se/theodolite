package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class LinearSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        for (res in resources) {

            logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
            if (this.benchmarkExecutor.runExperiment(load, res)) return res
        }
        return null
    }
}
