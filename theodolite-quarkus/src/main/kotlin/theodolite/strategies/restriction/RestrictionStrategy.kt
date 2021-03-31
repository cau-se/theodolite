package theodolite.strategies.restriction

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

/**
 * A 'Restriction Strategy' restricts a list of resources based on the current
 * results of all previously performed benchmarks.
 *
 * @param results the result object
 *
 * @see Results
 */
@RegisterForReflection
abstract class RestrictionStrategy(val results: Results) {
    /**
     * Apply the restriction of the given resource list for the given load based on the results object.
     *
     * @param load Load dimension for which a subset of resources are required.
     * @param resources List of resources to be restricted.
     * @return Returns a list containing only elements that have not been filtered out by the
     * restriction (possibly empty).
     *
     * @see LoadDimension
     * @see Resource
     * @see Results
     */
    abstract fun apply(load: LoadDimension, resources: List<Resource>): List<Resource>
}
