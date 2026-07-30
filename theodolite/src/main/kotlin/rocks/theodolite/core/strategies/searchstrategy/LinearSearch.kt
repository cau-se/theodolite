package rocks.theodolite.core.strategies.searchstrategy

import mu.KotlinLogging
import rocks.theodolite.core.ExperimentRunner
import rocks.theodolite.core.SloExperimentResult

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest/biggest suitable number of resources/loads,
 *  depending on the metric.
 *
 * @param experimentRunner Benchmark executor which runs the individual benchmarks.
 */
class LinearSearch(experimentRunner: ExperimentRunner) : SearchStrategy(experimentRunner) {

    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {
        for (res in resources) {
            logger.info { "Running experiment with load '$load' and resources '$res'" }
            if (this.experimentRunner.runExperiment(load, res) == SloExperimentResult.SUCCESS) return res
        }
        return null
    }

    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {
        var maxSuitableLoad: Int? = null
        for (load in loads) {
            logger.info { "Running experiment with resources '$resource' and load '$load'" }
            if (this.experimentRunner.runExperiment(load, resource) == SloExperimentResult.SUCCESS) {
                maxSuitableLoad = load
            } else break
        }
        return maxSuitableLoad
    }
}
