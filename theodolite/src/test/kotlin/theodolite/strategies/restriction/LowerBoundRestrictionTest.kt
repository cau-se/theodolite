package theodolite.strategies.restriction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import theodolite.util.LoadDimension
import theodolite.util.Resource
import theodolite.util.Results

internal class LowerBoundRestrictionTest {

    @Test
    fun testNoPreviousResults() {
        val results = Results()
        val strategy = LowerBoundRestriction(results)
        val load = buildLoadDimension(10000)
        val resources = listOf(
            buildResourcesDimension(1),
            buildResourcesDimension(2),
            buildResourcesDimension(3)
        )
        val restriction = strategy.apply(load, resources)

        assertEquals(3, restriction.size)
        assertEquals(resources, restriction)
    }

    @Test
    fun testWithSuccessfulPreviousResults() {
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, true)
        val strategy = LowerBoundRestriction(results)
        val load = buildLoadDimension(30000)
        val resources = listOf(
            buildResourcesDimension(1),
            buildResourcesDimension(2),
            buildResourcesDimension(3)
        )
        val restriction = strategy.apply(load, resources)

        assertEquals(2, restriction.size)
        assertEquals(resources.subList(1, 3), restriction)
    }

    @Test
    @Disabled
    fun testWithNoSuccessfulPreviousResults() {
        // This test is currently not implemented this way, but might later be the desired behavior.
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, false)
        results.setResult(20000, 3, false)
        val strategy = LowerBoundRestriction(results)
        val load = buildLoadDimension(30000)
        val resources = listOf(
            buildResourcesDimension(1),
            buildResourcesDimension(2),
            buildResourcesDimension(3)
        )
        val restriction = strategy.apply(load, resources)

        assertEquals(0, restriction.size)
        assertEquals(emptyList<Resource>(), restriction)
    }


    @Test
    fun testNoPreviousResults2() {
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(20000, 2, true)
        results.setResult(10000, 1, false)
        results.setResult(20000, 2, true)

        val minRequiredInstances = results.getMinRequiredInstances(LoadDimension(20000, emptyList()))

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!.get())
    }

    @Test
    @Disabled
    fun testMinRequiredInstancesWhenNotSuccessful() {
        // This test is currently not implemented this way, but might later be the desired behavior.
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(20000, 2, true)
        results.setResult(10000, 1, false)
        results.setResult(20000, 2, false)

        val minRequiredInstances = results.getMinRequiredInstances(LoadDimension(20000, emptyList()))

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!.get())
    }

    private fun buildLoadDimension(load: Int): LoadDimension {
        return LoadDimension(load, emptyList())
    }

    private fun buildResourcesDimension(resources: Int): Resource {
        return Resource(resources, emptyList())
    }

    private fun Results.setResult(load: Int, resources: Int, successful: Boolean) {
        this.setResult(
            Pair(
                buildLoadDimension(load),
                buildResourcesDimension(resources)
            ),
            successful
        )
    }
}
