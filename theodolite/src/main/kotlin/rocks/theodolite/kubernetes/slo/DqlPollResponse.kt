package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {}

class DqlPollResponse (
    private val state : String,
    private val progress : Int,
    private val result : QueryResult
) : MetricQueryResponse {

    data class QueryResult (
        val records : List<ResultRecord>,
        private val types : List<Any>,
        private val metadata : Any
    ){

        data class ResultRecord (
            val timeframe: TimeFrame,
            val interval: Long, //in nanoseconds
        ){
            val grouping: MutableMap<String, String> = mutableMapOf()
            val metrics: MutableMap<String, List<Double?>> = mutableMapOf()

            @JsonAnySetter
            fun handleDynamicFields(key: String, value: JsonNode) {
                when {
                    value.isTextual -> {
                        grouping[key] = value.asText()
                    }
                    value.isArray -> {
                        val array = value as ArrayNode
                        val doubles = array.map { node ->
                            if (node.isNull) null else node.asDouble()
                        }
                        metrics[key] = doubles
                    }
                    else -> {
                        throw IllegalArgumentException("Unexpected value type for key '$key': $value")
                    }
                }
            }

            data class TimeFrame (
                val start : Instant,
                val end : Instant
            )
        }
    }

    companion object {
        fun fromString(json: String): DqlPollResponse {
            return jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(json)
        }
    }


    @JsonIgnore
    override fun getResultAsList(onlyFirst: Boolean): List<List<String>> {
        //    [[ group, timestamp, value ], [ group, timestamp, value ], ... ] to match PrometheusResponse
        val recordsToProcess = if (onlyFirst && result.records.isNotEmpty()) {
            result.records.take(1)
        } else {
            result.records
        }

        if (result.records.isNotEmpty() && result.records.first().metrics.values.size > 1)
            logger.warn { "DQL Query includes multiple metrics, only taking first for CSV" }

        val resultsList = recordsToProcess.flatMap { record ->
            val group = record.grouping.keys.joinToString(", ")
            val values = record.metrics.values.first()
            values.mapIndexed { index, value ->
                val timestamp = record.timeframe.start.plusNanos(index * record.interval)
                listOf(group, timestamp.toEpochMilli().toString(), value.toString())
            }
        }

        return resultsList
    }

    @JsonIgnore
    override fun isNullOrEmpty(): Boolean {
        return this.result.records.all { record ->
            record.metrics.values.all { list ->
                list.isEmpty() || list.all { it == null }
            }
        }
    }

    @JsonIgnore
    override fun getDataForSLOChecker(onlyFirst: Boolean): List<SloJson.MetricResult> {
        val records = if (onlyFirst && result.records.isNotEmpty()) result.records.take(1) else result.records
        if (result.records.isNotEmpty() && result.records.first().metrics.values.size > 1)
            logger.warn { "DQL Query includes multiple metrics, only taking first for SLO check" }
        return records.mapTo(mutableListOf()) { record ->
            val values = record.metrics.values.first()
            val resultsList = values.mapIndexedNotNull { index, value ->
                if (value != null) {
                    val timestamp = record.timeframe.start.plusNanos(index * record.interval)
                    listOf(timestamp.epochSecond, value.toString())
                } else {
                    null
                }
            }

            SloJson.MetricResult(
                metric = record.grouping,
                values = resultsList
            )
        }
    }

    @JsonIgnore
    fun isSuccessful() : Boolean {
        return this.state == "SUCCEEDED"
    }
}
