package rocks.theodolite.core

import rocks.theodolite.core.strategies.Metric

fun createResultsFromArray(array: Array<Array<Boolean>>, metric: Metric): Results {
    val results = Results(metric)
    for (load in array.indices) {
        for (resources in array[load].indices) {
            val result = if (array[load][resources]) SloExperimentResult.SUCCESS else SloExperimentResult.FAILURE
            results.addExperimentResult(Pair(load + 1, resources + 1), result)
        }
    }
    return results
}
