package rocks.theodolite.core

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import rocks.theodolite.core.strategies.Metric

@QuarkusTest
internal class ResultsTest {

    @Test
    fun testMinRequiredInstancesWhenSuccessfulDemand() {
        val results = Results(Metric.from("demand"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(10000, 2), true)
        results.setResult(Pair(20000, 1), false)
        results.setResult(Pair(20000, 2), true)

        val minRequiredInstances = results.getOptYDimensionValue(20000)

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances)
    }

    @Test
    fun testGetMaxBenchmarkedLoadWhenAllSuccessfulDemand() {
        val results = Results(Metric.from("demand"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(10000, 2), true)

        val test1 = results.getMaxBenchmarkedXDimensionValue(100000)

        assertNotNull(test1)
        assertEquals(10000, test1)
    }

    @Test
    fun testGetMaxBenchmarkedLoadWhenLargestNotSuccessfulDemand() {
        val results = Results(Metric.from("demand"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(10000, 2), true)
        results.setResult(Pair(20000, 1), false)

        val test2 = results.getMaxBenchmarkedXDimensionValue(100000)

        assertNotNull(test2)
        assertEquals(20000, test2)
    }

    @Test
    fun testMaxRequiredInstancesWhenSuccessfulCapacity() {
        val results = Results(Metric.from("capacity"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(20000, 1), false)
        results.setResult(Pair(10000, 2), true)
        results.setResult(Pair(20000, 2), true)

        val maxRequiredInstances = results.getOptYDimensionValue(2)

        assertNotNull(maxRequiredInstances)
        assertEquals(20000, maxRequiredInstances)
    }

    @Test
    fun testGetMaxBenchmarkedLoadWhenAllSuccessfulCapacity() {
        val results = Results(Metric.from("capacity"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(10000, 2), true)

        val test1 = results.getMaxBenchmarkedXDimensionValue(5)

        assertNotNull(test1)
        assertEquals(2, test1)
    }

    @Test
    fun testGetMaxBenchmarkedLoadWhenLargestNotSuccessfulCapacity() {
        val results = Results(Metric.from("capacity"))
        results.setResult(Pair(10000, 1), true)
        results.setResult(Pair(20000, 1), true)
        results.setResult(Pair(10000, 2), false)


        val test2 = results.getMaxBenchmarkedXDimensionValue(5)

        assertNotNull(test2)
        assertEquals(2, test2)
    }
}
