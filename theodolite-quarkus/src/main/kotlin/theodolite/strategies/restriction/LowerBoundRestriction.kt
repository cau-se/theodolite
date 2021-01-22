package theodolite.strategies.restriction

import theodolite.util.Results
import theodolite.util.LoadDimension
import theodolite.util.Resource

class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {
    override fun next(load: LoadDimension, resources: List<Resource>): List<Resource> {
            val maxLoad: LoadDimension? = this.results.getMaxBenchmarkedLoad(load)
            var lowerBound: Resource? = this.results.getMinRequiredInstances(maxLoad)
            if(lowerBound == null) {
                lowerBound = resources.get(0)
            }
            return resources.filter{x -> x.get() >= lowerBound.get()}
    }
}