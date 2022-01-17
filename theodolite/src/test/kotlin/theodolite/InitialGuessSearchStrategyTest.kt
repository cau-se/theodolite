package theodolite

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import theodolite.benchmark.BenchmarkExecution
import theodolite.strategies.searchstrategy.InitialGuessSearchStrategy
import theodolite.util.LoadDimension
import theodolite.util.Results
import mu.KotlinLogging
import theodolite.strategies.searchstrategy.PrevResourceMinGuess

private val logger = KotlinLogging.logger {}

@QuarkusTest
class InitialGuessSearchStrategyTest {

    @Test
    fun testInitialGuessSearch() {
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
        val guessStrategy = PrevResourceMinGuess()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 5)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor,guessStrategy, results)

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> = ArrayList(listOf(0, 2, 2, 3, 4, 6))
        expected.add(null)

        for (load in mockLoads) {
            val returnVal : Int? = strategy.findSuitableResource(load, mockResources)
            if(returnVal != null) {
                logger.info { "returnVal '${returnVal}'" }
            }
            else {
                logger.info { "returnVal is null." }
            }
            actual.add(returnVal)
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testInitialGuessSearchLowerResourceDemandHigherLoad() {
        val mockResults = arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, true, true, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        )
        val mockLoads: List<LoadDimension> = (0..6).map { number -> LoadDimension(number, emptyList()) }
        val mockResources: List<Int> = (0..6).toList()
        val results = Results()
        val benchmark = TestBenchmark()
        val guessStrategy = PrevResourceMinGuess()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 5)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor,guessStrategy, results)

        val actual: ArrayList<Int?> = ArrayList()
        val expected: ArrayList<Int?> = ArrayList(listOf(0, 2, 2, 1, 4, 6))
        expected.add(null)

        for (load in mockLoads) {
            val returnVal : Int? = strategy.findSuitableResource(load, mockResources)
            if(returnVal != null) {
                logger.info { "returnVal '${returnVal}'" }
            }
            else {
                logger.info { "returnVal is null." }
            }
            actual.add(returnVal)
        }

        assertEquals(actual, expected)
    }

    @Test
    fun testInitialGuessSearchFirstNotDoable() {
        val mockResults = arrayOf(
                arrayOf(false, false, false, false, false, false, false),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, false, true, true, true, true),
                arrayOf(true, true, true, true, true, true, true),
                arrayOf(false, false, false, false, true, true, true),
                arrayOf(false, false, false, false, false, false, true),
                arrayOf(false, false, false, false, false, false, false)
        )
        val mockLoads: List<LoadDimension> = (0..6).map { number -> LoadDimension(number, emptyList()) }
        val mockResources: List<Int> = (0..6).toList()
        val results = Results()
        val benchmark = TestBenchmark()
        val guessStrategy = PrevResourceMinGuess()
        val sloChecker: BenchmarkExecution.Slo = BenchmarkExecution.Slo()
        val benchmarkExecutor = TestBenchmarkExecutorImpl(mockResults, benchmark, results, listOf(sloChecker), 0, 0, 5)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor, guessStrategy, results)

        val actual: ArrayList<Int?> = ArrayList()
        var expected: ArrayList<Int?> = ArrayList(listOf(2, 3, 0, 4, 6))
        expected.add(null)
        expected = ArrayList(listOf(null) + expected)

        for (load in mockLoads) {
            val returnVal : Int? = strategy.findSuitableResource(load, mockResources)
            if(returnVal != null) {
                logger.info { "returnVal '${returnVal}'" }
            }
            else {
                logger.info { "returnVal is null." }
            }
            actual.add(returnVal)
        }

        assertEquals(actual, expected)
    }
}