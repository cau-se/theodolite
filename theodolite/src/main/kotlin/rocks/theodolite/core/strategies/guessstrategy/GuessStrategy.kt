package rocks.theodolite.core.strategies.guessstrategy

import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Base class for the implementation of Guess strategies. Guess strategies are strategies to determine the resource
 * demand (demand metric) or load (capacity metric) we start with in our initial guess search strategy.
 */

@RegisterForReflection
abstract class GuessStrategy {
    /**
     * Computing the resource demand (demand metric) or load (capacity metric) for the initial guess search strategy
     * to start with.
     *
     * @param valuesToCheck List of all possible resources/loads.
     * @param lastOptValue Previous minimal/maximal resource/load value for the given load/resource.
     *
     * @return the resource/load to start the initial guess search strategy with or null
     */
    abstract fun firstGuess(valuesToCheck: List<Int>, lastOptValue: Int?): Int?
}