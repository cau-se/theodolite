package rocks.theodolite.core

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.searchstrategy.SearchStrategy

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the possible loads of the execution
 * @param resources the possible resources of the execution
 * @param searchStrategy the [SearchStrategy] of the execution
 * @param metric the Metric of the execution
 */
@RegisterForReflection
data class Config(
        val loads: List<Int>,
        val resources: List<Int>,
        val searchStrategy: SearchStrategy,
        val metric: Metric
)
