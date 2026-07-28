package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@QuarkusTest
internal class DqlPollResponseTest {

    private val succeededJson = """
        {
          "state": "SUCCEEDED",
          "progress": 100,
          "result": {
            "records": [
              {
                "timeframe": {
                  "start": "2024-01-01T00:00:00Z",
                  "end":   "2024-01-01T00:01:00Z"
                },
                "interval": 10000000000,
                "k8s.namespace.name": "shop",
                "avg(dt.host.cpu.usage)": [0.1, 0.2, 0.3, null, 0.5]
              }
            ],
            "types": [],
            "metadata": {}
          }
        }
    """.trimIndent()

    private val runningJson = """
        {
          "state": "RUNNING",
          "progress": 50,
          "result": {
            "records": [],
            "types": [],
            "metadata": {}
          }
        }
    """.trimIndent()

    @Test
    fun testFromStringSucceeded() {
        val response = DqlPollResponse.fromString(succeededJson)
        assertInstanceOf(DqlPollResponse::class.java, response)
        assertTrue(response.isSuccessful())
    }

    @Test
    fun testFromStringRunning() {
        val response = DqlPollResponse.fromString(runningJson)
        assertFalse(response.isSuccessful())
    }

    @Test
    fun testIsNullOrEmptyFalseForSucceeded() {
        val response = DqlPollResponse.fromString(succeededJson)
        assertFalse(response.isNullOrEmpty())
    }

    @Test
    fun testIsNullOrEmptyTrueForEmptyRecords() {
        val response = DqlPollResponse.fromString(runningJson)
        assertTrue(response.isNullOrEmpty())
    }

    @Test
    fun testGetResultAsList() {
        val response = DqlPollResponse.fromString(succeededJson)
        val list = response.getResultAsList(onlyFirst = true)
        // 5 values in the array (including the null at index 3 which becomes "null") => 5 rows
        assertEquals(5, list.size)
        list.forEach { row ->
            assertEquals(3, row.size, "Each row should have [group, timestamp, value]")
        }
        assertEquals("k8s.namespace.name", list.first()[0])
    }

    @Test
    fun testGetDataForSloCheckerOnlyFirst() {
        val response = DqlPollResponse.fromString(succeededJson)
        val data = response.getDataForSLOChecker(onlyFirst = true)
        assertEquals(1, data.size)
        assertFalse(data.first().values.isNullOrEmpty())
    }

    @Test
    fun testImplementsMetricQueryResponse() {
        val response = DqlPollResponse.fromString(succeededJson)
        assertInstanceOf(MetricQueryResponse::class.java, response)
    }
}
