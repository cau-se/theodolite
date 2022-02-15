package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.Results

private val logger = KotlinLogging.logger {}

// TODO: Is actually just a heuristic approach. Not ensured to have opt solution. Maybe Talk to Sören about it.
/**
 *  Search strategy implementation for determining the smallest suitable resource demand.
 *  Starting with a resource amount provided by a guess strategy.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 * @param guessStrategy Strategy that provides us with a guess for the first resource amount.
 * @param results current results of all previously performed benchmarks.
 */
class InitialGuessSearchStrategy(benchmarkExecutor: BenchmarkExecutor, guessStrategy: GuessStrategy, results: Results) :
        SearchStrategy(benchmarkExecutor, guessStrategy, results) {

    override fun findSuitableResource(load: Int, resources: List<Int>): Int? {

        if(resources.isEmpty()) {
            logger.info { "You need to specify resources to be checked for the InitialGuessSearchStrategy to work." }
            return null
        }

        if(guessStrategy == null){
            logger.info { "Your InitialGuessSearchStrategy doesn't have a GuessStrategy. This is not supported." }
            return null
        }

        if(results == null){
            logger.info { "The results need to be initialized." }
            return null
        }

        var lastLowestResource : Int? = null

        // Getting the lastLowestResource from results and calling firstGuess() with it
        if (!results.isEmpty()) {
            val maxLoad: Int? = this.results.getMaxBenchmarkedXDimensionValue(load)
            lastLowestResource = this.results.getOptYDimensionValue(maxLoad)
        }
        lastLowestResource = this.guessStrategy.firstGuess(resources, lastLowestResource)

        if (lastLowestResource != null) {
            val resourcesToCheck: List<Int>
            val startIndex: Int = resources.indexOf(lastLowestResource)

            logger.info { "Running experiment with load '$load' and resources '$lastLowestResource'" }

            // If the first experiment passes, starting downward linear search
            // otherwise starting upward linear search
            if (this.benchmarkExecutor.runExperiment(load, lastLowestResource)) {

                resourcesToCheck = resources.subList(0, startIndex).reversed()
                if (resourcesToCheck.isEmpty()) return lastLowestResource

                var currentMin: Int = lastLowestResource
                for (res in resourcesToCheck) {

                    logger.info { "Running experiment with load '$load' and resources '$res'" }
                    if (this.benchmarkExecutor.runExperiment(load, res)) {
                        currentMin = res
                    }
                }
                return currentMin
            }
            else {
                if (resources.size <= startIndex + 1) {
                    logger.info{ "No more resources left to check." }
                    return null
                }
                resourcesToCheck = resources.subList(startIndex + 1, resources.size)

                for (res in resourcesToCheck) {

                    logger.info { "Running experiment with load '$load' and resources '$res'" }
                    if (this.benchmarkExecutor.runExperiment(load, res)) return res
                }
            }
        }
        else {
            logger.info { "lastLowestResource was null." }
        }
        return null
    }

    override fun findSuitableLoad(resource: Int, loads: List<Int>): Int? {

        if(loads.isEmpty()) {
            logger.info { "You need to specify resources to be checked for the InitialGuessSearchStrategy to work." }
            return null
        }

        if(guessStrategy == null){
            logger.info { "Your InitialGuessSearchStrategy doesn't have a GuessStrategy. This is not supported." }
            return null
        }

        if(results == null){
            logger.info { "The results need to be initialized." }
            return null
        }

        var lastMaxLoad : Int? = null

        // Getting the lastLowestLoad from results and calling firstGuess() with it
        if (!results.isEmpty()) {
            val maxResource: Int? = this.results.getMaxBenchmarkedXDimensionValue(resource)
            lastMaxLoad = this.results.getOptYDimensionValue(maxResource)
        }
        lastMaxLoad = this.guessStrategy.firstGuess(loads, lastMaxLoad)

        if (lastMaxLoad != null) {
            val loadsToCheck: List<Int>
            val startIndex: Int = loads.indexOf(lastMaxLoad)

            logger.info { "Running experiment with resource '$resource' and load '$lastMaxLoad'" }

            // If the first experiment passes, starting upwards linear search
            // otherwise starting downward linear search
            if (!this.benchmarkExecutor.runExperiment(resource, lastMaxLoad)) {
                // downward search

                loadsToCheck = loads.subList(0, startIndex).reversed()
                if (loadsToCheck.isNotEmpty()) {
                    for (load in loadsToCheck) {

                        logger.info { "Running experiment with resource '$resource' and load '$load'" }
                        if (this.benchmarkExecutor.runExperiment(resource, load)) {
                            return load
                        }
                    }
                }
            }
            else {
                // upward search
                if (loads.size <= startIndex + 1) {
                    return lastMaxLoad
                }
                loadsToCheck = loads.subList(startIndex + 1, loads.size)

                var currentMax: Int = lastMaxLoad
                for (load in loadsToCheck) {
                    logger.info { "Running experiment with resource '$resource' and load '$load'" }
                    if (this.benchmarkExecutor.runExperiment(resource, load)) {
                        currentMax = load
                    }
                }
                return currentMax
            }
        }
        else {
            logger.info { "lastMaxLoad was null." }
        }
        return null
    }
}