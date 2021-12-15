package theodolite.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.Resource
import theodolite.util.Results

/**
 * Base class for the implementation of Guess strategies. Guess strategies are strategies to determine the resource
 * demand we start with in our initial guess search strategy.
 */

@RegisterForReflection
abstract class GuessStrategy {
    /**
     * Computing the resource demand for the initial guess search strategy to start with.
     *
     * @param resources List of all possible [Resource]s.
     * @param lastLowestResource Previous resource demand needed for the given load.
     *
     * @return Returns the resource demand to start the initial guess search strategy with or null
     */
    abstract fun firstGuess(resources: List<Resource>, lastLowestResource: Resource?): Resource?
}