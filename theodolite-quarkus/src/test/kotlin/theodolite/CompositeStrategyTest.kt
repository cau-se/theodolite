package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.execution.TestBenchmarkExecutor
import theodolite.util.*

@QuarkusTest
class CompositeStrategyTest {

    @Test
    fun testEnd2EndLinearSearch() {
        val mockResults = arrayOf(
            arrayOf( true , true , true , true , true , true , true),
            arrayOf( false, false, true , true , true , true , true),
            arrayOf( false, false, true , true , true , true , true),
            arrayOf( false, false, false, true , true , true , true),
            arrayOf( false, false, false, false, true , true , true),
            arrayOf( false, false, false, false, false, false, true),
            arrayOf( false, false, false, false, false, false, false)
        )
        val mockLoads: List<LoadDimension> =  (0..6).map{number -> LoadDimension(number)}
        val mockResources: List<Resource> =  (0..6).map{number -> Resource(number)}
        val results: Results = Results();
        val benchmark = TestBenchmark()
        val benchmarkExecutor: TestBenchmarkExecutor = TestBenchmarkExecutor(mockResults, benchmark, results)
        val linearSearch: LinearSearch = LinearSearch(benchmarkExecutor, results);
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction), results)

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6).map{ x -> Resource(x)})
        expected.add(null)

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResources(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testEnd2EndBinarySearch() {
        val mockResults = arrayOf(
            arrayOf( true , true , true , true , true , true , true),
            arrayOf( false, false, true , true , true , true , true),
            arrayOf( false, false, true , true , true , true , true),
            arrayOf( false, false, false, true , true , true , true),
            arrayOf( false, false, false, false, true , true , true),
            arrayOf( false, false, false, false, false, false, true),
            arrayOf( false, false, false, false, false, false, false)
        )
        val mockLoads: List<LoadDimension> =  (0..6).map{number -> LoadDimension(number)}
        val mockResources: List<Resource> =  (0..6).map{number -> Resource(number)}
        val results: Results = Results();
        val benchmark = TestBenchmark()
        val benchmarkExecutor: TestBenchmarkExecutor = TestBenchmarkExecutor(mockResults, benchmark, results)
        val binarySearch: BinarySearch = BinarySearch(benchmarkExecutor, results);
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, binarySearch, setOf(lowerBoundRestriction), results) // sets instead of lists

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6).map{ x -> Resource(x)})
        expected.add(null)

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResources(load, mockResources))
        }

        assertEquals(actual, expected)
    }



}