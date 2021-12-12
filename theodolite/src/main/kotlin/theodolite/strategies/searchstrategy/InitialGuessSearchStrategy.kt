package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 *  Search strategy implementation for determining the smallest suitable resource demand.
 *  Starting with a resource amount provided by a guess strategy.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 * @param guessStrategy Strategy that provides us with a guess for the first resource amount.
 */
class InitialGuessSearchStrategy(benchmarkExecutor: BenchmarkExecutor, guessStrategy: GuessStrategy) : SearchStrategy(benchmarkExecutor, guessStrategy) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>, lastLowestResource: Resource?): Resource? {

        if(guessStrategy == null){
            logger.info { "Your InitialGuessSearchStrategy doesn't have a GuessStrategy. This is not supported." }
            return null
        }

        var lastLowestResourceToUse = this.guessStrategy.firstGuess(resources, lastLowestResource)

        if (lastLowestResourceToUse != null) {
            val resourcesToCheck: List<Resource>
            val startIndex: Int = resources.indexOf(lastLowestResourceToUse)

            logger.info { "Running experiment with load '${load.get()}' and resources '${lastLowestResourceToUse.get()}'" }

            // If the first experiment passes, starting downward linear search
            // otherwise starting upward linear search
            if (this.benchmarkExecutor.runExperiment(load, lastLowestResourceToUse)) {

                resourcesToCheck = resources.subList(0, startIndex).reversed()
                if (resourcesToCheck.isEmpty()) return lastLowestResourceToUse

                var currentMin: Resource = lastLowestResourceToUse
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