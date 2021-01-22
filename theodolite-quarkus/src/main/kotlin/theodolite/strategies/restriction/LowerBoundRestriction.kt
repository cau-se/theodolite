package theodolite.strategies.restriction

import theodolite.util.Results
import theodolite.util.LoadDimension
import theodolite.util.Resource
import kotlin.math.max

class LowerBoundRestriction(results: Results) : RestrictionStrategy(results) {
    override fun next(load: LoadDimension, resources: List<Resource>): List<Resource> {
            val maxLoad: LoadDimension? = this.results.getMaxBenchmarkedLoad(load)
            var lowerBound: Resource? = this.results.getRequiredInstances(maxLoad)
            if(lowerBound == null) {
                lowerBound = Resource(0) // TODO handle the null case
            }
            return resources.filter{x -> x.get() >= lowerBound.get()}
    }
}