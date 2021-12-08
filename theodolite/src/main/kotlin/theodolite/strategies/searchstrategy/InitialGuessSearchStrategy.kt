package theodolite.strategies.searchstrategy

import mu.KotlinLogging
import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

private val logger = KotlinLogging.logger {}

/**
 *  Linear-search-like implementation for determining the smallest suitable number of instances.
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 */
class InitialGuessSearchStrategy(benchmarkExecutor: BenchmarkExecutor) : SearchStrategy(benchmarkExecutor) {

    override fun findSuitableResource(load: LoadDimension, resources: List<Resource>, lastLowestResource: Resource?): Resource? {

        if (lastLowestResource != null) {
            val resourcesToCheck: List<Resource>
            val startIndex: Int = resources.indexOf(lastLowestResource)

            logger.info { "Running experiment with load '${load.get()}' and resources '${lastLowestResource.get()}'" }

            if (this.benchmarkExecutor.runExperiment(load, lastLowestResource)) {

                resourcesToCheck = resources.subList(0, startIndex).reversed()
                if(resourcesToCheck.isEmpty()) return lastLowestResource

                var currentMin : Resource = lastLowestResource
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