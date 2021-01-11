package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor) {
    abstract fun findSuitableResources(load: LoadDimension, resources: List<Resource>): Resource?;
}