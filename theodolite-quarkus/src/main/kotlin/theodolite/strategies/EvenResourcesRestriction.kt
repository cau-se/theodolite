package theodolite.strategies

import theodolite.strategies.restriction.CompositeRestriction
import theodolite.strategies.restriction.Restriction

class EvenResourcesRestriction(childRestriction: Restriction) : CompositeRestriction(childRestriction) {
    override fun restrict(loads: List<Int>, resources: List<Int>) {
        val filteredResources: List<Int> = resources.filter { x -> x % 2 == 0 };
        this.childRestriction.restrict(loads, filteredResources);
    }

}