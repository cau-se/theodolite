package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import java.lang.IllegalArgumentException

/**
 *  Search for the smallest suitable resource with binary search.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class BinarySearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {
    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        val result = binarySearch(load, resources, 0, resources.size - 1)
        if( result == -1 ) {
            return null
        }
        return resources[result]
    }

    private fun binarySearch (load: LoadDimension, resources: List<Resource>, lower: Int, upper: Int): Int {
        if (lower > upper) {
            throw IllegalArgumentException()
        }
        // special case:  length == 1 or 2
        if (lower == upper) {
            if (this.benchmarkExecutor.runExperiment(load, resources[lower])) return lower
            else {
                if (lower + 1 == resources.size) return - 1
                return lower + 1
            }
        } else {
            // apply binary search for a list with length > 2 and adjust upper and lower depending on the result for `resources[mid]`
            val mid = (upper + lower) / 2
            if (this.benchmarkExecutor.runExperiment(load, resources[mid])) {
                if (mid == lower) {
                    return lower
                }
                return binarySearch(load, resources, lower, mid - 1 )
            } else {
              return binarySearch(load, resources, mid + 1 , upper)
            }
        }
    }

}