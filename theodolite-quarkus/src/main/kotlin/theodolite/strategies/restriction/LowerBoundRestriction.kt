package theodolite.strategies.restriction

import theodolite.util.Results
import theodolite.util.LoadDimension
import theodolite.util.Resource

class LowerBoundRestriction(results: Results, loads: List<LoadDimension>) : RestrictionStrategy(results, loads) {
    override fun next(load: LoadDimension, resources: List<Resource>): List<Resource> {
            var lowerBound: Resource? = this.results.getRequiredInstances(load)
            if(lowerBound == null) {
                lowerBound = Resource(0) // TODO handle the null case
            }
            return resources.filter{x -> x.get() >= lowerBound.get()}
    }
}