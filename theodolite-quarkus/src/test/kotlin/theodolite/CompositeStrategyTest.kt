package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.execution.TestBenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

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
        val benchmarkExecutor: TestBenchmarkExecutor = TestBenchmarkExecutor(mockResults)
        val linearSearch: LinearSearch = LinearSearch(benchmarkExecutor);
        val results: Results = Results();
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results, mockLoads);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, linearSearch, listOf(lowerBoundRestriction))

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
        val benchmarkExecutor: TestBenchmarkExecutor = TestBenchmarkExecutor(mockResults)
        val binarySearch: BinarySearch = BinarySearch(benchmarkExecutor);
        val results: Results = Results();
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results, mockLoads);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, binarySearch, listOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6).map{ x -> Resource(x)})
        expected.add(null)

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResources(load, mockResources))
        }

        assertEquals(actual, expected)
    }



}