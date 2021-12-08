package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 *  Search strategy implementation for determining the smallest suitable number of instances, which takes the
 *  resource demand of the previous load into account as a starting point for the search.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class InitialGuessSearchStrategy(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>, lastLowestResource: Resource?): Resource? {

        var lastLowestResourceToUse = lastLowestResource

        // This Search strategy needs a resource demand to start the search with,
        // if this is not provided it will be set to the first resource of the given resource-list
        if(lastLowestResource == null && resources.isNotEmpty()){
            lastLowestResourceToUse = resources[0]
        }

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