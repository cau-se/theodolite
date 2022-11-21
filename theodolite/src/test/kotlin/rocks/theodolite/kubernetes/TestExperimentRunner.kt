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
        val result = this.mockResults.getResult(load, resource) ?: throw IllegalStateException("Result is null.")
        this.results.setResult(Pair(load, resource), result)
        return result
    }
}
