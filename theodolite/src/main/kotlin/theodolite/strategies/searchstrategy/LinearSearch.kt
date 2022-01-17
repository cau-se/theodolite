package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resources

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class LinearSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Int>): Int? {
        for (res in resources) {
            logger.info { "Running experiment with load '${load.get()}' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(load, res)) return res
        }
        return null
    }

    // Stops after having the first load which is not possible anymore with the current resource, maybe some later load still possible tho
    // kinda like GuessSearchStrat case -> differentiate or is it fine like that?
    override fun findSuitableLoad(resource: Int, loads: List<LoadDimension>): LoadDimension? {
        var maxSuitableLoad: LoadDimension? = null
        for (load in loads) {
            logger.info { "Running experiment with resources '$resource' and load '${load.get()}'" }
            if (this.benchmarkExecutor.runExperiment(load, resource)) {
                maxSuitableLoad = load
            } else break
        }
        return maxSuitableLoad
    }
}
