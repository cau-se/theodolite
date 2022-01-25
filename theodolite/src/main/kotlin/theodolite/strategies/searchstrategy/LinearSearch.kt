package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class LinearSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {
        for (res in resources) {
            logger.info { "Running experiment with load '$load' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(load, res)) return res
        }
        return null
    }

    // Stops after having the first load which is not possible anymore with the current resource, maybe some later load still possible tho
    // kinda like GuessSearchStrat case -> differentiate or is it fine like that?
    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {
        var maxSuitableLoad: Int? = null
        for (load in loads) {
            logger.info { "Running experiment with resources '$resource' and load '$load'" }
            if (this.benchmarkExecutor.runExperiment(load, resource)) {
                maxSuitableLoad = load
            } else break
        }
        return maxSuitableLoad
    }
}
