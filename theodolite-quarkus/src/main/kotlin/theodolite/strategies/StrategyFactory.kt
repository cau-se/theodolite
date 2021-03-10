package theodolite.strategies

import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.SearchStrategy
import theodolite.util.Results

class StrategyFactory {

    fun createSearchStrategy(executor: BenchmarkExecutor, searchStrategyString: String): SearchStrategy {
        return when (searchStrategyString) {
            "LinearSearch" -> LinearSearch(executor)
            "BinarySearch" -> BinarySearch(executor)
            else -> throw IllegalArgumentException("Search Strategy $searchStrategyString not found")
        }
    }

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
