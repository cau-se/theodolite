package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 * [SearchStrategy] that executes experiment for provides resources in a linear-search-like fashion, but **without
 * stopping** once a suitable resource amount is found.
 *
 * @see LinearSearch for a SearchStrategy that stops once a suitable resource amount is found.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class FullSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        var minimalSuitableResources: Resource? = null
        for (res in resources) {
            logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
            val result = this.benchmarkExecutor.runExperiment(load, res)
            if (result && minimalSuitableResources != null) {
                minimalSuitableResources = res
            }
        }
        return minimalSuitableResources
    }
}
