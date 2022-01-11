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
            //TODO: that actually doesnt make sense no? Shouldnt it be == null?
            if (result && minimalSuitableResources != null) {
                minimalSuitableResources = res
            }
        }
        return minimalSuitableResources
    }

    override fun findSuitableLoad(resource: Resource, loads: List<LoadDimension>): LoadDimension? {
        var maxSuitableLoad: LoadDimension? = null
        for (load in loads) {
            logger.info { "Running experiment with resources '${resource.get()}' and load '${load.get()}'" }
            if (this.benchmarkExecutor.runExperiment(load, resource)) maxSuitableLoad = load
        }
        return maxSuitableLoad
    }
}
