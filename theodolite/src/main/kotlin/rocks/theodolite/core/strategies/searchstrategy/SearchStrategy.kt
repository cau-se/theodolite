package rocks.theodolite.core.strategies.searchstrategy

import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.kubernetes.execution.BenchmarkExecutor

/**
 *  Base class for the implementation for SearchStrategies. SearchStrategies determine the smallest suitable number
 *  of resources/loads for a load/resource (depending on the metric).
 *
 * @param benchmarkExecutor Benchmark executor which runs the individual benchmarks.
 * @param guessStrategy Guess strategy for the initial resource amount in case the InitialGuessStrategy is selected.
 * @param results the [Results] object.
 */
@RegisterForReflection
abstract class SearchStrategy(val benchmarkExecutor: BenchmarkExecutor) {


    /**
     * Calling findSuitableResource or findSuitableLoad for each load/resource depending on the chosen metric.
     *
     * @param loads List of possible loads for the experiments.
     * @param resources List of possible resources for the experiments.
     * @param metric The [Metric] for the experiments, either "demand" or "capacity".
     */
    fun applySearchStrategyByMetric(loads: List<Int>, resources: List<Int>, metric: Metric) {

        when(metric) {
            Metric.DEMAND ->
                for (load in loads) {
                    if (benchmarkExecutor.run.get()) {
                        this.findSuitableResource(load, resources)
                    }
                }
            Metric.CAPACITY ->
                for (resource in resources) {
                    if (benchmarkExecutor.run.get()) {
                        this.findSuitableLoad(resource, loads)
                    }
                }
        }
    }

    /**
     * Find the smallest suitable resource from the specified resource list for the given load.
     *
     * @param load the load to be tested.
     * @param resources List of all possible resources.
     *
     * @return suitable resource for the specified load, or null if no suitable resource exists.
     */
    abstract fun findSuitableResource(load: Int, resources: List<Int>): Int?

    /**
     * Find the biggest suitable load from the specified load list for the given resource amount.
     *
     * @param resource the resource to be tested.
     * @param loads List of all possible loads.
     *
     * @return suitable load for the specified resource amount, or null if no suitable load exists.
     */
    abstract fun findSuitableLoad(resource: Int, loads: List<Int>) : Int?
}
