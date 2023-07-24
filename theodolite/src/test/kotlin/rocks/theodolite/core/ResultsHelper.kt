package rocks.theodolite.core

import rocks.theodolite.core.strategies.Metric

fun createResultsFromArray(array: Array<Array<Boolean>>, metric: Metric): Results {
    val results = Results(metric)
    for (load in array.indices) {
        for (resources in array[load].indices) {
            results.addExperimentResult(Pair(load + 1, resources + 1), array[load][resources])
        }
    }
    return results
}
