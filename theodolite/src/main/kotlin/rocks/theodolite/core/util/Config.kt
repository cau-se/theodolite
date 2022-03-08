package rocks.theodolite.core.util

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.searchstrategy.SearchStrategy
import rocks.theodolite.kubernetes.util.PatcherDefinition

/**
 * Config class that represents a configuration of a theodolite run.
 *
 * @param loads the possible loads of the execution
 * @param resources the possible resources of the execution
 * @param searchStrategy the [SearchStrategy] of the execution
 * @param metric the Metric of the execution
 */
@RegisterForReflection
data class Config(
        val loads: List<Int>,
        val loadPatcherDefinitions : List<PatcherDefinition>,
        val resources: List<Int>,
        val resourcePatcherDefinitions : List<PatcherDefinition>,
        val searchStrategy: SearchStrategy,
        val metric: Metric
)
