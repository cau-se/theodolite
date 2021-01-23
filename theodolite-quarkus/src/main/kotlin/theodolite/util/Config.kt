package theodolite.util

import theodolite.strategies.restriction.RestrictionStrategy
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.SearchStrategy
import java.time.Duration

data class Config(
    val loads: List<LoadDimension>,
    val resources: List<Resource>,
    val compositeStrategy: CompositeStrategy,
    val executionDuration: Duration
) {

}