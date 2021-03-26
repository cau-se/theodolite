package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

@RegisterForReflection
abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor) {
    /**
     * Find smallest suitable resource from the specified resource list for the given load.
     *
     * @param load Load to be tested.
     * @param resources List of all possible resources.
     * @return suitable resource for the specified load, or null if no suitable resource exists.
     */
    abstract fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource?
}
