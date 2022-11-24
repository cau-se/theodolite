package rocks.theodolite.core.strategies.searchstrategy

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.TestExperimentRunner
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.restrictionstrategy.LowerBoundRestriction
import rocks.theodolite.core.Results
import rocks.theodolite.core.createResultsFromArray

@QuarkusTest
class RestrictionSearchTest {

    @Test
    fun restrictionSearchNoMatch() {
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true),
            arrayOf(false, false),
            arrayOf(true, true),
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..3).toList()
        val mockResources: List<Int> = (1..2).toList()
        val results = Results(Metric.DEMAND)
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val linearSearch = LinearSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction))

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, null, null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }

    @Test
    fun restrictionSearchTestLinearSearch() {
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..7).toList()
        val results = Results(Metric.DEMAND)
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val linearSearch = LinearSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction))

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, 3, 3, 4, 5, 7, null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }

    @Test
    fun restrictionSearchTestFullSearch() {
        val mockResults = createResultsFromArray(arrayOf(
                arrayOf(true, true, true, true, true, true, true),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, false, true, true, true, true),
                arrayOf(false, false, false, false, true, true, true),
                arrayOf(false, false, false, false, false, false, true),
                arrayOf(false, false, false, false, false, false, false)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..7).toList()
        val results = Results(Metric.DEMAND)
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val fullSearch = FullSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutor, fullSearch, setOf(lowerBoundRestriction))

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, 3, 3, 4, 5, 7, null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }

    @Test
    fun restrictionSearchTestBinarySearch() {
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..7).toList()
        val results = Results(Metric.DEMAND)
        val benchmarkExecutorImpl = TestExperimentRunner(results, mockResults)
        val binarySearch = BinarySearch(benchmarkExecutorImpl)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutorImpl, binarySearch, setOf(lowerBoundRestriction))

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, 3, 3, 4, 5, 7, null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }

    @Test
    fun restrictionSearchTestBinarySearch2() {
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true, true),
            arrayOf(false, false, false, true, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true, true),
            arrayOf(false, false, false, false, false, false, true, true),
            arrayOf(false, false, false, false, false, false, false, true)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..8).toList()
        val results = Results(Metric.DEMAND)
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val binarySearch = BinarySearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy = RestrictionSearch(benchmarkExecutor, binarySearch, setOf(lowerBoundRestriction))

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int> = listOf(1, 3, 3, 4, 5, 7, 8)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }
}
