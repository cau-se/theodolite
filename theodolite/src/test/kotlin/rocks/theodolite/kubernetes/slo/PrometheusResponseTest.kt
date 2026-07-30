package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class PrometheusResponseTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun testSingleTimeseriesListConvert() {
        val prometheusResponse = readSingleTimeseriesResponse()
        val resultsList = prometheusResponse.getResultAsList()
        assertEquals(126, resultsList.size)
    }

    @Test
    fun testSingleTimeseriesWithNoMillisListConvert() {
        val prometheusResponse = readSingleTimeseriesNoMillisResponse()
        val resultsList = prometheusResponse.getResultAsList()
        assertEquals(11, resultsList.size)
    }

    @Test
    fun testNoTimeseriesListConvert() {
        val prometheusResponse = readNoTimeseriesResponse()
        val resultsList = prometheusResponse.getResultAsList()
        assertEquals(0, resultsList.size)
    }

    @Test
    fun testMultiTimeseriesListConvertWithOnlyFirst() {
        val prometheusResponse = readMultiTimeseriesResponse()
        val resultsList = prometheusResponse.getResultAsList()
        assertEquals(17, resultsList.size)
    }

    @Test
    fun testMultiTimeseriesListConvertWithoutOnlyFirst() {
        val prometheusResponse = readMultiTimeseriesResponse()
        val resultsList = prometheusResponse.getResultAsList(onlyFirst = false)
        assertEquals(1700, resultsList.size)
    }

    private fun readMultiTimeseriesResponse(): PrometheusResponse {
        return objectMapper.readValue(
                javaClass.getResourceAsStream("/prometheus-response/multi-timeseries.json"),
                PrometheusResponse::class.java
        )
    }

    private fun readSingleTimeseriesResponse(): PrometheusResponse {
        return objectMapper.readValue(
                javaClass.getResourceAsStream("/prometheus-response/single-timeseries.json"),
                PrometheusResponse::class.java
        )
    }

    private fun readSingleTimeseriesNoMillisResponse(): PrometheusResponse {
        return objectMapper.readValue(
                javaClass.getResourceAsStream("/prometheus-response/single-timeseries-nomillis.json"),
                PrometheusResponse::class.java
        )
    }

    private fun readNoTimeseriesResponse(): PrometheusResponse {
        return objectMapper.readValue(
                javaClass.getResourceAsStream("/prometheus-response/empty-timeseries.json"),
                PrometheusResponse::class.java
        )
    }

}