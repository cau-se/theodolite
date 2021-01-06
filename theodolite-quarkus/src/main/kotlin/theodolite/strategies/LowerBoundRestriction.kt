package theodolite.strategies

import theodolite.strategies.restriction.PrimitiveRestriction
import theodolite.strategies.searchstrategy.SearchStrategy

class LowerBoundRestriction(searchStrategy: SearchStrategy) : PrimitiveRestriction(searchStrategy) {
    override fun restrict(loads: List<Int>, resources: List<Int>): List<Int> {
        val lowerBounds: MutableList<Int> = ArrayList<Int>();
        var lowerBound = 0;
        for (load in loads) {
            lowerBound = this.searchStrategy.findSuitableResources(load, resources.subList(lowerBound, resources.size));
            lowerBounds.add(lowerBound);
        }
        return lowerBounds;
    }
}