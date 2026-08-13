package rocks.theodolite.core

import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.searchstrategy.SearchStrategy


class ExecutionRunner(
    private val searchStrategy: SearchStrategy,
    private val resources: List<Int>, private val loads: List<Int>,
    private val metric: Metric, private val executionId: Int
) {

    /**
     * Run all experiments for given loads and resources.
     * Called by [rocks.theodolite.kubernetes.execution.TheodoliteExecutor] to run an Execution.
     */
    fun run() {

        val ioHandler = IOHandler()
        val resultsFolder = ioHandler.getResultFolderURL()

        //execute benchmarks for each load for the demand metric, or for each resource amount for capacity metric
        try {
            searchStrategy.applySearchStrategyByMetric(loads, resources, metric)

        } finally {
            ioHandler.writeToJSONFile(
                searchStrategy.experimentRunner.results,
                "${resultsFolder}exp${executionId}-result.json"
            )
            // Create expXYZ_demand.csv file or expXYZ_capacity.csv depending on metric
            when (metric) {
                Metric.DEMAND ->
                    ioHandler.writeToCSVFile(
                        "${resultsFolder}exp${executionId}_demand",
                        calculateMetric(loads, searchStrategy.experimentRunner.results),
                        listOf("load", "resources")
                    )

                Metric.CAPACITY ->
                    ioHandler.writeToCSVFile(
                        "${resultsFolder}exp${executionId}_capacity",
                        calculateMetric(resources, searchStrategy.experimentRunner.results),
                        listOf("resource", "loads")
                    )
            }
        }
    }

    private fun calculateMetric(xValues: List<Int>, results: Results): List<List<String>> {
        return xValues.map { listOf(it.toString(), results.getOptimalYValue(it).toString()) }
    }
}