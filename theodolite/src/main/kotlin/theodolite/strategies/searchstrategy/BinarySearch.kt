package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor

private val logger = KotlinLogging.logger {}

/**
 *  Binary-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class BinarySearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {
    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {
        val result = binarySearchDemand(load, resources, 0, resources.size - 1)
        if (result == -1) {
            return null
        }
        return resources[result]
    }

    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {
        val result = binarySearchCapacity(resource, loads, 0, loads.size - 1)
        if (result == -1) {
            return null
        }
        return loads[result]
    }

    /**
     * Apply binary search for metric demand.
     *
     * @param load the load dimension to perform experiments for
     * @param resources the list in which binary search is performed
     * @param lower lower bound for binary search (inclusive)
     * @param upper upper bound for binary search (inclusive)
     */
    private fun binarySearchDemand(load: Int, resources: List<Int>, lower: Int, upper: Int): Int {
        if (lower > upper) {
            throw IllegalArgumentException()
        }
        // special case:  length == 1, so lower and upper bounds are the same
        if (lower == upper) {
            val res = resources[lower]
            logger.info { "Running experiment with load '${load}' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(load, resources[lower])) return lower
            else {
                if (lower + 1 == resources.size) return -1
                return lower + 1
            }
        } else {
            // apply binary search for a list with
            // length >= 2 and adjust upper and lower depending on the result for `resources[mid]`
            val mid = (upper + lower) / 2
            val res = resources[mid]
            logger.info { "Running experiment with load '${load}' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(load, resources[mid])) {
                // case length = 2
                if (mid == lower) {
                    return lower
                }
                return binarySearchDemand(load, resources, lower, mid - 1)
            } else {
                return binarySearchDemand(load, resources, mid + 1, upper)
            }
        }
    }


    /**
     * Apply binary search for metric capacity.
     *
     * @param resource the load dimension to perform experiments for
     * @param loads the list in which binary search is performed
     * @param lower lower bound for binary search (inclusive)
     * @param upper upper bound for binary search (inclusive)
     */
    private fun binarySearchCapacity(resource: Int, loads: List<Int>, lower: Int, upper: Int): Int {
        if (lower > upper) {
            throw IllegalArgumentException()
        }
        // length = 1, so lower and upper bounds are the same
        if (lower == upper) {
            val res = loads[lower]
            logger.info { "Running experiment with load '$resource' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(resource, loads[lower])) return lower
            else {
                if (lower + 1 == loads.size) return -1
                return lower - 1
            }
        } else {
            // apply binary search for a list with
            // length > 2 and adjust upper and lower depending on the result for `resources[mid]`
            val mid = (upper + lower + 1) / 2 //round to next int
            val res = loads[mid]
            logger.info { "Running experiment with load '$resource' and resources '$res'" }
            if (this.benchmarkExecutor.runExperiment(resource, loads[mid])) {
                // length = 2, so since we round down mid is equal to lower
                if (mid == upper) {
                    return upper
                }
                return binarySearchCapacity(resource, loads, mid + 1, upper)
            } else {
                return binarySearchCapacity(resource, loads, lower, mid - 1)
            }
        }
    }
}
