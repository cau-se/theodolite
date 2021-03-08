package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import theodolite.strategies.searchstrategy.LinearSearch
import theodolite.strategies.searchstrategy.BinarySearch
import theodolite.strategies.restriction.LowerBoundRestriction
import theodolite.strategies.searchstrategy.CompositeStrategy
import theodolite.util.*
import java.util.*

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
        val mockLoads: List<LoadDimension> =  (0..6).map{number -> LoadDimension(number, "NumSensors")}
        val mockResources: List<Resource> =  (0..6).map{number -> Resource(number, "Instances")}
        val results: Results = Results();
        val benchmark = TestBenchmark()
        val benchmarkExecutor: TestBenchmarkExecutorImpl = TestBenchmarkExecutorImpl(mockResults, benchmark, results)
        val linearSearch: LinearSearch = LinearSearch(benchmarkExecutor);
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, linearSearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6).map{ x -> Resource(x, "Instances")})
        expected.add(null)

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
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
        val mockLoads: List<LoadDimension> =  (0..6).map{number -> LoadDimension(number, "NumSensors")}
        val mockResources: List<Resource> =  (0..6).map{number -> Resource(number, "Instances")}
        val results: Results = Results();
        val benchmark = TestBenchmark()
        val benchmarkExecutorImpl: TestBenchmarkExecutorImpl = TestBenchmarkExecutorImpl(mockResults, benchmark, results)
        val binarySearch: BinarySearch = BinarySearch(benchmarkExecutorImpl);
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutorImpl, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6).map{ x -> Resource(x, "Instances")})
        expected.add(null)

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testEnd2EndBinarySearch2() {
        val mockResults = arrayOf(
            arrayOf( true , true , true , true , true , true , true, true),
            arrayOf( false, false, true , true , true , true , true, true),
            arrayOf( false, false, true , true , true , true , true, true),
            arrayOf( false, false, false, true , true , true , true, true),
            arrayOf( false, false, false, false, true , true , true, true),
            arrayOf( false, false, false, false, false, false, true, true),
            arrayOf( false, false, false, false, false, false, false, true)
        )
        val mockLoads: List<LoadDimension> =  (0..6).map{number -> LoadDimension(number, "NumSensors")}
        val mockResources: List<Resource> =  (0..7).map{number -> Resource(number, "Instances")}
        val results: Results = Results();
        val benchmark = TestBenchmark()
        val benchmarkExecutor: TestBenchmarkExecutorImpl = TestBenchmarkExecutorImpl(mockResults, benchmark, results)
        val binarySearch: BinarySearch = BinarySearch(benchmarkExecutor);
        val lowerBoundRestriction: LowerBoundRestriction = LowerBoundRestriction(results);
        val strategy: CompositeStrategy = CompositeStrategy(benchmarkExecutor, binarySearch, setOf(lowerBoundRestriction))

        val actual: ArrayList<Resource?> = ArrayList<Resource?>()
        val expected: ArrayList<Resource?> = ArrayList(listOf(0,2,2,3,4,6,7).map{ x -> Resource(x, "Instances")})

        for(load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(actual, expected)
    }

}