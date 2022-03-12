package theodolite

import rocks.theodolite.core.Results
import rocks.theodolite.kubernetes.benchmark.Benchmark
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo
import rocks.theodolite.core.ExperimentRunner

class TestExperimentRunnerImpl(
        results: Results,
        private val mockResults: Array<Array<Boolean>>,
        private val benchmark: Benchmark,
        private val slo: List<Slo>,
        private val executionId: Int,
        private val loadGenerationDelay: Long,
        private val afterTeardownDelay: Long
) : ExperimentRunner(
        results
) {

    override fun runExperiment(load: Int, resource: Int): Boolean {
        val result = this.mockResults[load][resource]
        this.results.setResult(Pair(load, resource), result)
        return result
    }
}
