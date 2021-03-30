package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.searchstrategy.CompositeStrategy

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the LoadDimensions of the execution
 * @param resources the Resources of the execution
 * @param compositeStrategy the CompositeStrategy of the execution
 *
 * @see LoadDimension
 * @see Resource
 * @see CompositeStrategy
 */
@RegisterForReflection
data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy
)
