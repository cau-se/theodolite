package rocks.theodolite.core.strategies.searchstrategy

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.benchmark.TestBenchmarkDeploymentBuilder
import rocks.theodolite.kubernetes.execution.TestExperimentRunnerImpl

import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.restrictionstrategy.LowerBoundRestriction
import rocks.theodolite.core.Results
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo

@QuarkusTest
class RestrictionSearchTest {


    @Test
    fun restrictionSearchTestLinearSearch() {
        val mockResults = arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        )
        val mockLoads: List<Int> = (0..6).toList()
        val mockResources: List<Int> = (0..6).toList()
        val results = Results(Metric.from("demand"))
        val benchmarkDeploymentBuilder = TestBenchmarkDeploymentBuilder()
        val sloChecker: Slo = Slo()
        val benchmarkExecutor = TestExperimentRunnerImpl(results, mockResults, benchmarkDeploymentBuilder, listOf(sloChecker), 0, 0, 5)
        val linearSearch = LinearSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
            RestrictionSearch(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> = ArrayList(listOf(0, 2, 2, 3, 4, 6))
        expected.add(null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun restrictionSearchTestFullSearch() {
        val mockResults = arrayOf(
                arrayOf(true, true, true, true, true, true, true),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, false, true, true, true, true),
                arrayOf(false, false, false, false, true, true, true),
                arrayOf(false, false, false, false, false, false, true),
                arrayOf(false, false, false, false, false, false, false)
        )
        val mockLoads: List<Int> = (0..6).toList()
        val mockResources: List<Int> = (0..6).toList()
        val results = Results(Metric.from("demand"))
        val benchmarkDeploymentBuilder = TestBenchmarkDeploymentBuilder()
        val sloChecker: Slo = Slo()
        val benchmarkExecutor = TestExperimentRunnerImpl(results, mockResults, benchmarkDeploymentBuilder, listOf(sloChecker), 0, 0, 5)
        val fullSearch = FullSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
                RestrictionSearch(benchmarkExecutor, fullSearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> = ArrayList(listOf(0, 2, 2, 3, 4, 6))
        expected.add(null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun restrictionSearchTestBinarySearch() {
        val mockResults = arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        )
        val mockLoads: List<Int> = (0..6).toList()
        val mockResources: List<Int> = (0..6).toList()
        val results = Results(Metric.from("demand"))
        val benchmarkDeploymentBuilder = TestBenchmarkDeploymentBuilder()
        val sloChecker: Slo = Slo()
        val benchmarkExecutorImpl =
            TestExperimentRunnerImpl(results, mockResults, benchmarkDeploymentBuilder, listOf(sloChecker), 0, 0, 0)
        val binarySearch = BinarySearch(benchmarkExecutorImpl)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutorImpl, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> = ArrayList(listOf(0, 2, 2, 3, 4, 6))
        expected.add(null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun restrictionSearchTestBinarySearch2() {
        val mockResults = arrayOf(
            arrayOf(true, true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, false, false, true, true),
            arrayOf(false, false, false, false, false, false, false, true)
        )
        val mockLoads: List<Int> = (0..6).toList()
        val mockResources: List<Int> = (0..7).toList()
        val results = Results(Metric.from("demand"))
        val benchmarkDeploymentBuilder = TestBenchmarkDeploymentBuilder()
        val sloChecker: Slo = Slo()
        val benchmarkExecutor = TestExperimentRunnerImpl(results, mockResults, benchmarkDeploymentBuilder, listOf(sloChecker), 0, 0, 0)
        val binarySearch = BinarySearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
            RestrictionSearch(benchmarkExecutor, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> =
            ArrayList(listOf(0, 2, 2, 3, 4, 6, 7))

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }
}
