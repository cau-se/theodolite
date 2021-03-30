package theodolite.strategies

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.SearchStrategy
import theodolite.util.Results

/**
 * Factory for creating SearchStrategy and RestrictionStrategy Strategies.
 *
 * @see SearchStrategy
 * @see RestrictionStrategy
 */
class StrategyFactory {

    /**
     * Create a search strategy.
     *
     * @param executor The @see BenchmarkExecutor that executes individual experiments.
     * @param searchStrategyString Specifies the @see SearchStrategy. Must either be the string 'LinearSearch',
     * or 'BinarySearch'.
     *
     * @throws IllegalArgumentException if @param searchStrategyString was not one of the allowed options.
     */
    fun createSearchStrategy(executor: BenchmarkExecutor, searchStrategyString: String): SearchStrategy {
        return when (searchStrategyString) {
            "LinearSearch" -> LinearSearch(executor)
            "BinarySearch" -> BinarySearch(executor)
            else -> throw IllegalArgumentException("Search Strategy $searchStrategyString not found")
        }
    }

    /**
     * Create a restriction strategy.
     *
     * @param results The @see Results saves the state of the theodolite benchmark run.
     * @param restrictionStrings Specifies the list of RestrictionStrategy that are used to restrict the amount
     * of @see theodolite.util.Resource for a fixed LoadDimension. Must equal the string
     * 'LowerBound'.
     *
     * @throws IllegalArgumentException if param searchStrategyString was not one of the allowed options.
     *
     * @see SearchStrategy
     * @see RestrictionStrategy
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
