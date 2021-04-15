package theodolite.strategies.restriction

import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

/**
 * A 'Restriction Strategy' restricts a list of resources based on the current
 * results of all previously performed benchmarks.
 *
 * @param results the [Results] object
 */
@RegisterForReflection
abstract class RestrictionStrategy(val results: Results) {
    /**
     * Apply the restriction of the given resource list for the given load based on the results object.
     *
     * @param load [LoadDimension] for which a subset of resources are required.
     * @param resources List of [Resource]s to be restricted.
     * @return Returns a list containing only elements that have not been filtered out by the
     * restriction (possibly empty).
     */
    abstract fun apply(load: LoadDimension, resources: List<Resource>): List<Resource>
}
