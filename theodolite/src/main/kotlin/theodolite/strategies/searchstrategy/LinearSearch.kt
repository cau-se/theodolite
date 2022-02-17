package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest/biggest suitable number of resources/loads,
 *  depending on the metric.
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
