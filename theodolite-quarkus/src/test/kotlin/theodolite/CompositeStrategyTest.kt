package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import theodolite.benchmark.BenchmarkExecution
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

@QuarkusTest
class CompositeStrategyTest {

    @Test
    fun testEnd2EndLinearSearch() {
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
        val mockResources: List<Resource> = (0..6).map { number -> Resource(number, emptyList()) }
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, sloChecker, 0, 0, 5)
        val linearSearch = LinearSearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
            CompositeStrategy(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0, 2, 2, 3, 4, 6).map { x -> Resource(x, emptyList()) })
        expected.add(null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testEnd2EndBinarySearch() {
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
        val mockResources: List<Resource> = (0..6).map { number -> Resource(number, emptyList()) }
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutorImpl =
            TestBenchmarkExecutorImpl(mockResults, benchmark, results, sloChecker, 0, 0, 0)
        val binarySearch = BinarySearch(benchmarkExecutorImpl)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
            CompositeStrategy(benchmarkExecutorImpl, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0, 2, 2, 3, 4, 6).map { x -> Resource(x, emptyList()) })
        expected.add(null)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testEnd2EndBinarySearch2() {
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
        val mockResources: List<Resource> = (0..7).map { number -> Resource(number, emptyList()) }
        val results = Results()
        val benchmark = TestBenchmark()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, sloChecker, 0, 0, 0)
        val binarySearch = BinarySearch(benchmarkExecutor)
        val lowerBoundRestriction = LowerBoundRestriction(results)
        val strategy =
            CompositeStrategy(benchmarkExecutor, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList()
        val expected: ArrayList<Resource?> =
            ArrayList(listOf(0, 2, 2, 3, 4, 6, 7).map { x -> Resource(x, emptyList()) })

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }
}
