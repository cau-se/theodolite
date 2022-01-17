package theodolite.strategies

import theodolite.benchmark.BenchmarkExecution
import theodolite.execution.BenchmarkExecutor
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.*
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
    fun createSearchStrategy(executor: BenchmarkExecutor, searchStrategyObject: BenchmarkExecution.Strategy, results: Results): SearchStrategy {

        var strategy : SearchStrategy = when (searchStrategyObject.name) {
            "FullSearch" -> FullSearch(executor)
            "LinearSearch" -> LinearSearch(executor)
            "BinarySearch" -> BinarySearch(executor)
            "RestrictionSearch" -> when (searchStrategyObject.searchStrategy){
                //TODO: Do we only need LinearSearch here as valid searchstrat? Or actually just allow all?
                // If we dont have restriction Strat specified but still RestrictionSearch just do normal Search
                "FullSearch" -> composeSearchRestrictionStrategy(executor, FullSearch(executor), results, searchStrategyObject.restrictions)
                "LinearSearch" -> composeSearchRestrictionStrategy(executor, LinearSearch(executor), results, searchStrategyObject.restrictions)
                "BinarySearch" -> composeSearchRestrictionStrategy(executor, BinarySearch(executor), results, searchStrategyObject.restrictions)
                else -> throw IllegalArgumentException("Search Strategy ${searchStrategyObject.searchStrategy} for RestrictionSearch not found")
            }
            "InitialGuessSearch" -> when (searchStrategyObject.guessStrategy){
                "PrevResourceMinGuess" -> InitialGuessSearchStrategy(executor,PrevResourceMinGuess(), results)
                else -> throw IllegalArgumentException("Guess Strategy ${searchStrategyObject.guessStrategy} not found")
            }
            else -> throw IllegalArgumentException("Search Strategy $searchStrategyObject not found")
        }

        return strategy
    }

    /**
     * Create a [RestrictionStrategy].
     *
     * @param results The [Results] saves the state of the Theodolite benchmark run.
     * @param restrictionStrings Specifies the list of [RestrictionStrategy] that are used to restrict the amount
     * of [theodolite.util.Resources] for a fixed LoadDimension. Must equal the string
     * 'LowerBound'.
     *
     * @throws IllegalArgumentException if param searchStrategyString was not one of the allowed options.
     */
    private fun createRestrictionStrategy(results: Results, restrictionStrings: List<String>): Set<RestrictionStrategy> {
        return restrictionStrings
            .map { restriction ->
                when (restriction) {
                    "LowerBound" -> LowerBoundRestriction(results)
                    else -> throw IllegalArgumentException("Restriction Strategy $restrictionStrings not found")
                }
            }.toSet()
    }

    /**
     * Create a RestrictionSearch, if the provided restriction list is not empty. Otherwise just return the given
     * searchStrategy.
     *
     * @param executor The [theodolite.execution.BenchmarkExecutor] that executes individual experiments.
     * @param searchStrategy The [SearchStrategy] to use
     * @param results The [Results] saves the state of the Theodolite benchmark run.
     * @param restrictions The [RestrictionStrategy]'s to use
     */
    private fun composeSearchRestrictionStrategy(executor: BenchmarkExecutor, searchStrategy: SearchStrategy,
                                                 results: Results, restrictions: List<String>): SearchStrategy {
        if(restrictions.isNotEmpty()){
            return RestrictionSearch(executor,searchStrategy,createRestrictionStrategy(results, restrictions))
        }
        return searchStrategy
    }
}
