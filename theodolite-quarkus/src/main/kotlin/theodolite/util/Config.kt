package theodolite.util

import theodolite.strategies.searchstrategy.CompositeStrategy

data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy
)
