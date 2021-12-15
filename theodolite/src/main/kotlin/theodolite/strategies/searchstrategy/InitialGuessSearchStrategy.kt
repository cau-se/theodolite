package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

private val logger = KotlinLogging.logger {}

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

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>): Resource? {

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


        var lastLowestResource : Resource? = null

        // Getting the lastLowestResource from results and calling firstGuess() with it
        if (!results.isEmpty()) {
            val maxLoad: LoadDimension? = this.results.getMaxBenchmarkedLoad(load)
            lastLowestResource = this.results.getMinRequiredInstances(maxLoad)
            if (lastLowestResource.get() == Int.MAX_VALUE) lastLowestResource = null
        }
        lastLowestResource = this.guessStrategy.firstGuess(resources, lastLowestResource)

        if (lastLowestResource != null) {
            val resourcesToCheck: List<Resource>
            val startIndex: Int = resources.indexOf(lastLowestResource)

            logger.info { "Running experiment with load '${load.get()}' and resources '${lastLowestResource.get()}'" }

            // If the first experiment passes, starting downward linear search
            // otherwise starting upward linear search
            if (this.benchmarkExecutor.runExperiment(load, lastLowestResource)) {

                resourcesToCheck = resources.subList(0, startIndex).reversed()
                if (resourcesToCheck.isEmpty()) return lastLowestResource

                var currentMin: Resource = lastLowestResource
                for (res in resourcesToCheck) {

                    logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
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

                    logger.info { "Running experiment with load '${load.get()}' and resources '${res.get()}'" }
                    if (this.benchmarkExecutor.runExperiment(load, res)) return res
                }
            }
        }
        else {
            logger.info { "InitialGuessSearchStrategy called without lastLowestResource value, which is needed as a " +
                    "starting point!" }
        }
        return null
    }
}