package rocks.theodolite.kubernetes

import rocks.theodolite.core.Results
import rocks.theodolite.core.ExperimentRunner

class TestExperimentRunner(
        results: Results,
        private val mockResults: Results
) : ExperimentRunner(
        results
) {

    override fun runExperiment(load: Int, resource: Int): Boolean {
        val result = this.mockResults.getExperimentResult(load, resource) ?: throw IllegalStateException("Result is null.")
        this.results.addExperimentResult(Pair(load, resource), result)
        return result
    }
}
