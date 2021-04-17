package theodolite.util

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ResultsTest {

    @Test
    fun testMinRequiredInstancesWhenSuccessful() {
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

    private fun Results.setResult(load: Int, resources: Int, successful: Boolean) {
        this.setResult(
            Pair(
                LoadDimension(load, emptyList()),
                Resource(resources, emptyList())
            ),
            successful)
    }

}