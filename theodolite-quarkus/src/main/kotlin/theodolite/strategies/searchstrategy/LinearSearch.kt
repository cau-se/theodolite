package theodolite.strategies.searchstrategy

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

class LinearSearch(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {
        for (res in resources) {
            if (this.benchmarkExecutor.runExperiment(load, res)) return res
        }
        return null
    }
}