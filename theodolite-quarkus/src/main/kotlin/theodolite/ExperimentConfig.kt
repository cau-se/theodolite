package theodolite

import theodolite.strategies.SearchAlgorithm

data class ExperimentConfig(
    val useCase: Int,
    val expId: Int,
    val dimValues: Array<Int>,
    val replicass: Array<Int>,
    val partitions: Int,
    val cpuLimit: String,
    val memoryLimit: String,
    val executionMinutes: Float,
    val prometheusBaseUrl: String,
    val reset: Boolean,
    val namespace: String,
    val resultPath: String,
    val configurations: Any,
    val restrictionStrategy: SearchAlgorithm,
    val searchStrategy: SearchStrategy,
    val benchmark: Any, // TODO
    val sloChecker: Any // TODO
) {


}