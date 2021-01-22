package theodolite.util

import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.SearchStrategy

data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy
) {

}