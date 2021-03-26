package theodolite.util

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.strategies.searchstrategy.CompositeStrategy

@RegisterForReflection
data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy
)
