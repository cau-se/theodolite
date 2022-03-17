package rocks.theodolite.kubernetes.execution

import rocks.theodolite.kubernetes.benchmark.TestBenchmarkDeploymentBuilder
import rocks.theodolite.core.Results
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo
import rocks.theodolite.core.ExperimentRunner

class TestExperimentRunnerImpl(
        results: Results,
        private val mockResults: Array<Array<Boolean>>,
        private val benchmarkDeploymentBuilder: TestBenchmarkDeploymentBuilder,
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
