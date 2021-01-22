package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor, val results: Results) {
    abstract fun findSuitableResources(load: LoadDimension, resources: List<Resource>): Resource?;
}