package theodolite.strategies.searchstrategy

abstract class SearchStrategy(val benchmark: Benchmark) {
    abstract fun findSuitableResources(load: Int, resources: List<Int>): Int;
}