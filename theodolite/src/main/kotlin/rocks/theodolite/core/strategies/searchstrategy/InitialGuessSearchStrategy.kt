package rocks.theodolite.core.strategies.searchstrategy

import mu.KotlinLogging
import rocks.theodolite.core.ExperimentRunner
import rocks.theodolite.core.Results
import rocks.theodolite.core.strategies.guessstrategy.GuessStrategy

private val logger = KotlinLogging.logger {}

/**
 *  Search strategy implementation for determining the smallest suitable resource demand.
 *  Starting with a resource amount provided by a guess strategy.
 *
 * @param experimentRunner Benchmark executor which runs the individual benchmarks.
 * @param guessStrategy Strategy that provides us with a guess for the first resource amount.
 * @param results current results of all previously performed benchmarks.
 */
class InitialGuessSearchStrategy(
    experimentRunner: ExperimentRunner,
    private val guessStrategy: GuessStrategy,
    private var results: Results
) : SearchStrategy(experimentRunner) {

    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {

        var lastLowestResource: Int? = null

        // Getting the lastLowestResource from results and calling firstGuess() with it
        if (!results.isEmpty()) {
            val maxLoad: Int? = this.results.getPreviousXValue(load)
            lastLowestResource = this.results.getOptimalYValue(maxLoad)
        }
        lastLowestResource = this.guessStrategy.firstGuess(resources, lastLowestResource)

        if (lastLowestResource != null) {
            val resourcesToCheck: List<Int>
            val startIndex: Int = resources.indexOf(lastLowestResource)

            logger.info { "Running experiment with load '$load' and resources '$lastLowestResource'" }

            // If the first experiment passes, starting downward linear search
            // otherwise starting upward linear search
            if (this.experimentRunner.runExperiment(load, lastLowestResource)) {

                resourcesToCheck = resources.subList(0, startIndex).reversed()
                if (resourcesToCheck.isEmpty()) return lastLowestResource

                var currentMin: Int = lastLowestResource
                for (res in resourcesToCheck) {

                    logger.info { "Running experiment with load '$load' and resources '$res'" }
                    if (this.experimentRunner.runExperiment(load, res)) {
                        currentMin = res
                    }
                }
                return currentMin
            } else {
                if (resources.size <= startIndex + 1) {
                    logger.info { "No more resources left to check." }
                    return null
                }
                resourcesToCheck = resources.subList(startIndex + 1, resources.size)

                for (res in resourcesToCheck) {

                    logger.info { "Running experiment with load '$load' and resources '$res'" }
                    if (this.experimentRunner.runExperiment(load, res)) return res
                }
            }
        } else {
            logger.info { "lastLowestResource was null." }
        }
        return null
    }

    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {

        var lastMaxLoad: Int? = null

        // Getting the lastLowestLoad from results and calling firstGuess() with it
        if (!results.isEmpty()) {
            val maxResource: Int? = this.results.getPreviousXValue(resource)
            lastMaxLoad = this.results.getOptimalYValue(maxResource)
        }
        lastMaxLoad = this.guessStrategy.firstGuess(loads, lastMaxLoad)

        if (lastMaxLoad != null) {
            val loadsToCheck: List<Int>
            val startIndex: Int = loads.indexOf(lastMaxLoad)

            logger.info { "Running experiment with resource '$resource' and load '$lastMaxLoad'" }

            // If the first experiment passes, starting upwards linear search
            // otherwise starting downward linear search
            if (!this.experimentRunner.runExperiment(lastMaxLoad, resource)) {
                // downward search

                loadsToCheck = loads.subList(0, startIndex).reversed()
                if (loadsToCheck.isNotEmpty()) {
                    for (load in loadsToCheck) {

                        logger.info { "Running experiment with resource '$resource' and load '$load'" }
                        if (this.experimentRunner.runExperiment(load, resource)) {
                            return load
                        }
                    }
                }
            } else {
                // upward search
                if (loads.size <= startIndex + 1) {
                    return lastMaxLoad
                }
                loadsToCheck = loads.subList(startIndex + 1, loads.size)

                var currentMax: Int = lastMaxLoad
                for (load in loadsToCheck) {
                    logger.info { "Running experiment with resource '$resource' and load '$load'" }
                    if (this.experimentRunner.runExperiment(load, resource)) {
                        currentMax = load
                    }
                }
                return currentMax
            }
        } else {
            logger.info { "lastMaxLoad was null." }
        }
        return null
    }
}