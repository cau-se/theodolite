package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor

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

    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {
        var minimalSuitableResources: Int? = null
        for (res in resources) {
            logger.info { "Running experiment with load '$load' and resources '$res'" }
            val result = this.benchmarkExecutor.runExperiment(load, res)
            if (result && minimalSuitableResources == null) {
                minimalSuitableResources = res
            }
        }
        return minimalSuitableResources
    }

    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {
        var maxSuitableLoad: Int? = null
        for (load in loads) {
            logger.info { "Running experiment with resources '$resource' and load '$load'" }
            if (this.benchmarkExecutor.runExperiment(load, resource)) maxSuitableLoad = load
        }
        return maxSuitableLoad
    }
}
