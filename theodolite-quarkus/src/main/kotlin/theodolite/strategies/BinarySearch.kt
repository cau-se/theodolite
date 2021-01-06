package theodolite.strategies

import theodolite.strategies.searchstrategy.Benchmark
import theodolite.strategies.searchstrategy.SearchStrategy

class BinarySearch(benchmark: Benchmark) : SearchStrategy(benchmark) {
    override fun findSuitableResources(load: Int, resources: List<Int>): Int {

    }
}