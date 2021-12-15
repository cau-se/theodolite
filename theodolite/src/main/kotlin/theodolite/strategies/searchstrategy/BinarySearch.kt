package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 *  Binary-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class BinarySearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {
    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        val result = binarySearch(load, resources, 0, resources.size - 1)
        if (result == -1) {
            return null
        }
        return resources[result]
    }

    /**
     * Apply binary search.
     *
     * @param load the load dimension to perform experiments for
     * @param resources the list in which binary search is performed
     * @param lower lower bound for binary search (inclusive)
     * @param upper upper bound for binary search (inclusive)
     */
    private fun binarySearch(load: LoadDimension, resources: List<Resource>, lower: Int, upper: Int): Int {
        if (lower > upper) {
            throw IllegalArgumentException()
        }
        // special case:  length == 1 or 2
        if (lower == upper) {
            val res = resources[lower]
            logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
            if (this.benchmarkExecutor.runExperiment(load, resources[lower])) return lower
            else {
                if (lower + 1 == resources.size) return -1
                return lower + 1
            }
        } else {
            // apply binary search for a list with
            // length > 2 and adjust upper and lower depending on the result for `resources[mid]`
            val mid = (upper + lower) / 2
            val res = resources[mid]
            logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
            if (this.benchmarkExecutor.runExperiment(load, resources[mid])) {
                if (mid == lower) {
                    return lower
                }
                return binarySearch(load, resources, lower, mid - 1)
            } else {
                return binarySearch(load, resources, mid + 1, upper)
            }
        }
    }
}
