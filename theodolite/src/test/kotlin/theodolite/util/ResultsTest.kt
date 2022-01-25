package theodolite.util

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ResultsTest {

    @Test
    fun testMinRequiredInstancesWhenSuccessful() {
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(10000, 2, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, true)

        val minRequiredInstances = results.getMinRequiredInstances(20000)

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!)
    }

    @Test
    @Disabled
    fun testMinRequiredInstancesWhenNotSuccessful() {
        // This test is currently not implemented this way, but might later be the desired behavior.
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(10000, 2, true)
        results.setResult(20000, 1, false)
        results.setResult(20000, 2, false)

        val minRequiredInstances = results.getMinRequiredInstances(20000)

        assertNotNull(minRequiredInstances)
        assertEquals(2, minRequiredInstances!!)
    }

    private fun Results.setResult(load: Int, resource: Int, successful: Boolean) {
        this.setResult(Pair(load, resource), successful)
    }


    @Test
    fun testGetMaxBenchmarkedLoadWhenAllSuccessful() {
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(10000, 2, true)

        val test1 = results.getMaxBenchmarkedLoad(100000)!!

        assertEquals(10000, test1)
    }

    @Test
    fun testGetMaxBenchmarkedLoadWhenLargestNotSuccessful() {
        val results = Results()
        results.setResult(10000, 1, true)
        results.setResult(10000, 2, true)
        results.setResult(20000, 1, false)

        val test2 = results.getMaxBenchmarkedLoad(100000)!!

        assertEquals(20000, test2)
    }
}
