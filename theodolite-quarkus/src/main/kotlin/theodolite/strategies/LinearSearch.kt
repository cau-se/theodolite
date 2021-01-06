package theodolite.strategies

import theodolite.strategies.searchstrategy.Benchmark
import theodolite.strategies.searchstrategy.SearchStrategy

class LinearSearch(benchmark: Benchmark) : SearchStrategy(benchmark) {

    override fun findSuitableResources(load: Int, resources: List<Int>): Int {
        for (res in resources) {
            if (this.benchmark.execute(load, res)) return resources.indexOf(res);
        }
        return resources.size;
    }
}