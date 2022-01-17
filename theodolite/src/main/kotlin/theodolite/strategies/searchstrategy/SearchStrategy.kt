package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resources
import theodolite.util.Results

/**
 *  Base class for the implementation for SearchStrategies. SearchStrategies determine the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 * @param guessStrategy Guess strategy for the initial resource amount in case the InitialGuessStrategy is selected.
 * @param results the [Results] object.
 */
@RegisterForReflection
abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor, val guessStrategy: GuessStrategy? = null,
                              val results: Results? = null) {
    /**
     * Find smallest suitable resource from the specified resource list for the given load.
     *
     * @param load the [LoadDimension] to be tested.
     * @param resources List of all possible [Resource]s.
     *
     * @return suitable resource for the specified load, or null if no suitable resource exists.
     */
    abstract fun findSuitableResource(load: LoadDimension, resources: List<Int>): Int?

    /**
     * Find biggest suitable load from the specified load list for the given resource amount.
     *
     * @param resource the [Resource] to be tested.
     * @param loads List of all possible [LoadDimension]s.
     *
     * @return suitable load for the specified resource amount, or null if no suitable load exists.
     */
    abstract fun findSuitableLoad(resource: Int, loads: List<LoadDimension>) : LoadDimension?
}
