package theodolite.strategies.restriction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import rocks.theodolite.core.strategies.Metric
import rocks.theodolite.core.strategies.restrictionstrategy.LowerBoundRestriction
import rocks.theodolite.core.util.Results

internal class LowerBoundRestrictionTest {

    @Test
    fun testNoPreviousResults() {
        val results = Results(Metric.from("demand"))
        val strategy = LowerBoundRestriction(results)
        val load = 10000
        val resources = listOf(1, 2, 3)
        val restriction = strategy.apply(load, resources)

        assertEquals(3, restriction.size)
        assertEquals(resources, restriction)
    }

    @Test
    fun testWithSuccessfulPreviousResults() {
        val results = Results(Metric.from("demand"))
        results.setResult(10000, 1, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, true)
        val strategy = LowerBoundRestriction(results)
        val load = 30000
        val resources = listOf(1, 2, 3)
        val restriction = strategy.apply(load, resources)

        assertEquals(2, restriction.size)
        assertEquals(resources.subList(1, 3), restriction)
    }

    @Test
    @Disabled
    fun testWithNoSuccessfulPreviousResults() {
        // This test is currently not implemented this way, but might later be the desired behavior.
        val results = Results(Metric.from("demand"))
        results.setResult(10000, 1, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, false)
        results.setResult(20000, 3, false)
        val strategy = LowerBoundRestriction(results)
        val load = 30000
        val resources = listOf(1, 2, 3)
        val restriction = strategy.apply(load, resources)

        assertEquals(0, restriction.size)
        assertEquals(emptyList<Int>(), restriction)
    }


    @Test
    fun testNoPreviousResults2() {
        val results = Results(Metric.from("demand"))
        results.setResult(10000, 1, true)
        results.setResult(20000, 2, true)
        results.setResult(10000, 1, false)
        results.setResult(20000, 2, true)

        val minRequiredInstances = results.getOptYDimensionValue(20000)

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!)
    }

    @Test
    @Disabled
    fun testMinRequiredInstancesWhenNotSuccessful() {
        // This test is currently not implemented this way, but might later be the desired behavior.
        val results = Results(Metric.from("demand"))
        results.setResult(10000, 1, true)
        results.setResult(20000, 2, true)
        results.setResult(10000, 1, false)
        results.setResult(20000, 2, false)

        val minRequiredInstances = results.getOptYDimensionValue(20000)

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!)
    }



    private fun Results.setResult(load: Int, resource: Int, successful: Boolean) {
        this.setResult(Pair(load, resource),successful)
    }
}
