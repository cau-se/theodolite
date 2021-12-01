package theodolite.strategies

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.FullSearch
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.SearchStrategy
import theodolite.util.Results

/**
 * Factory for creating [SearchStrategy] and [RestrictionStrategy] strategies.
 */
class StrategyFactory {

    /**
     * Create a [SearchStrategy].
     *
     * @param executor The [theodolite.execution.BenchmarkExecutor] that executes individual experiments.
     * @param searchStrategyString Specifies the [SearchStrategy]. Must either be the string 'LinearSearch',
     * or 'BinarySearch'.
     *
     * @throws IllegalArgumentException if the [SearchStrategy] was not one of the allowed options.
     */
    fun createSearchStrategy(executor: BenchmarkExecutor, searchStrategyString: String): SearchStrategy {
        return when (searchStrategyString) {
            "FullSearch" -> FullSearch(executor)
            "LinearSearch" -> LinearSearch(executor)
            "BinarySearch" -> BinarySearch(executor)
            else -> throw IllegalArgumentException("Search Strategy $searchStrategyString not found")
        }
    }

    /**
     * Create a [RestrictionStrategy].
     *
     * @param results The [Results] saves the state of the Theodolite benchmark run.
     * @param restrictionStrings Specifies the list of [RestrictionStrategy] that are used to restrict the amount
     * of [theodolite.util.Resource] for a fixed LoadDimension. Must equal the string
     * 'LowerBound'.
     *
     * @throws IllegalArgumentException if param searchStrategyString was not one of the allowed options.
     */
    fun createRestrictionStrategy(results: Results, restrictionStrings: List<String>): Set<RestrictionStrategy> {
        return restrictionStrings
            .map { restriction ->
                when (restriction) {
                    "LowerBound" -> LowerBoundRestriction(results)
                    else -> throw IllegalArgumentException("Restriction Strategy $restrictionStrings not found")
                }
            }.toSet()
    }
}
