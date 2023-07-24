package rocks.theodolite.core.strategies.searchstrategy

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rocks.theodolite.core.strategies.Metric
import mu.KotlinLogging
import rocks.theodolite.kubernetes.TestExperimentRunner
import rocks.theodolite.core.strategies.guessstrategy.PrevInstanceOptGuess
import rocks.theodolite.core.Results
import rocks.theodolite.core.createResultsFromArray
import rocks.theodolite.core.strategies.restrictionstrategy.LowerBoundRestriction

private val logger = KotlinLogging.logger {}

@QuarkusTest
class InitialGuessSearchStrategyTest {

    @Test
    fun initialGuessSearchNoMatch() {
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true),
            arrayOf(false, false),
            arrayOf(true, true),
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..3).toList()
        val mockResources: List<Int> = (1..2).toList()
        val results = Results(Metric.DEMAND)
        val guessStrategy = PrevInstanceOptGuess()
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor,guessStrategy, results)

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, null, 1)

        for (load in mockLoads) {
            actual.add(strategy.findSuitableResource(load, mockResources))
        }

        assertEquals(expected, actual)
    }

    @Test
    fun testInitialGuessSearch() {
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
        val guessStrategy = PrevInstanceOptGuess()
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor,guessStrategy, results)

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, 3, 3, 4, 5, 7, null)

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
        val mockResults = createResultsFromArray(arrayOf(
            arrayOf(true, true, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, false, true, true, true, true, true),
            arrayOf(false, true, true, true, true, true, true),
            arrayOf(false, false, false, false, true, true, true),
            arrayOf(false, false, false, false, false, false, true),
            arrayOf(false, false, false, false, false, false, false)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..7).toList()
        val results = Results(Metric.DEMAND)
        val guessStrategy = PrevInstanceOptGuess()
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor,guessStrategy, results)

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(1, 3, 3, 2, 5, 7, null)

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
        val mockResults = createResultsFromArray(arrayOf(
                arrayOf(false, false, false, false, false, false, false),
                arrayOf(false, false, true, true, true, true, true),
                arrayOf(false, false, false, true, true, true, true),
                arrayOf(true, true, true, true, true, true, true),
                arrayOf(false, false, false, false, true, true, true),
                arrayOf(false, false, false, false, false, false, true),
                arrayOf(false, false, false, false, false, false, false)
        ), Metric.DEMAND)
        val mockLoads: List<Int> = (1..7).toList()
        val mockResources: List<Int> = (1..7).toList()
        val results = Results(Metric.DEMAND)
        val guessStrategy = PrevInstanceOptGuess()
        val benchmarkExecutor = TestExperimentRunner(results, mockResults)
        val strategy = InitialGuessSearchStrategy(benchmarkExecutor, guessStrategy, results)

        val actual: MutableList<Int?> = mutableListOf()
        val expected: List<Int?> = listOf(null, 3, 4, 1, 5, 7, null)

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