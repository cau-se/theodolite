package theodolite.strategies.restriction

import theodolite.util.Results
import theodolite.util.LoadDimension
import theodolite.util.Resource


/**
 * A "Restriction Strategy" restricts a list of resources based on the current results of all previously performed benchmarks.
 */
abstract class RestrictionStrategy(val results: Results) {
    /**
     * Next Restrict the given resource list for the given load based on the result object.
     *
     * @param load Load dimension for which a subset of resources are required.
     * @param resources List of resources to be restricted.
     * @return Returns a list containing only elements that have not been filtered out by the restriction (possibly empty).
     */
    abstract fun next(load: LoadDimension, resources: List<Resource>): List<Resource>
}