package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

class LinearSearch(benchmarkExecutor: BenchmarkExecutor, results: Results) : SearchStrategy(benchmarkExecutor, results) {

    override fun findSuitableResources(load: LoadDimension, resources: List<Resource>): Resource? {
        for (res in resources) {
            if (this.benchmarkExecutor.runExperiment(load, res)) {
                this.results.setResult(Pair(load, res), true)
                return res
            } else {
                this.results.setResult(Pair(load, res), false)
            }
        }
        return null;
    }
}