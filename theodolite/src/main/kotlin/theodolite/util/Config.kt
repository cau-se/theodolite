package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.searchstrategy.SearchStrategy

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the [LoadDimension] of the execution
 * @param resources the [Resources] of the execution
 * @param searchStrategy the [SearchStrategy] of the execution
 * @param metric the Metric of the execution
 */
@RegisterForReflection
data class Config(
        val loads: List<LoadDimension>,
        val resources: Resources,
        val searchStrategy: SearchStrategy,
        val metric: String
)
