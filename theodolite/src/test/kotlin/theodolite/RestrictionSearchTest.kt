package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import theodolite.benchmark.BenchmarkExecution
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.RestrictionSearch
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.LoadDimension
import theodolite.util.Results

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
        val mockLoads: List<LoadDimension> = (0..6).map { number -> LoadDimension(number, emptyList()) }
        val mockResources: List<Int> = (0..6).toList()
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 5)
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
        val mockLoads: List<LoadDimension> = (0..6).map { number -> LoadDimension(number, emptyList()) }
        val mockResources: List<Int> = (0..6).toList()
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutorImpl =
            TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 0)
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
        val mockLoads: List<LoadDimension> = (0..6).map { number -> LoadDimension(number, emptyList()) }
        val mockResources: List<Int> = (0..7).toList()
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 0)
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
