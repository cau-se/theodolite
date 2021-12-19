package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.searchstrategy.RestrictionSearch
import theodolite.strategies.searchstrategy.SearchStrategy

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the [LoadDimension] of the execution
 * @param resources the [Resource] of the execution
 * @param searchStrategy the [SearchStrategy] of the execution
 */
@RegisterForReflection
data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val searchStrategy: SearchStrategy
)
