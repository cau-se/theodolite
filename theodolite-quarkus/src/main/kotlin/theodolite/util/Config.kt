package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.searchstrategy.CompositeStrategy

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the [LoadDimension] of the execution
 * @param resources the [Resource] of the execution
 * @param compositeStrategy the [CompositeStrategy] of the execution
 */
@RegisterForReflection
data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy
)
