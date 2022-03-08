package theodolite.model.crd

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import io.fabric8.kubernetes.api.model.MicroTime
import io.fabric8.kubernetes.api.model.Duration as K8sDuration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.model.crd.ExecutionState
import rocks.theodolite.kubernetes.model.crd.ExecutionStatus
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId


internal class ExecutionStatusTest {

    @Test
    fun testDefaultStateSerialization() {
        val objectMapper = ObjectMapper()
        val executionStatus = ExecutionStatus()
        val jsonString = objectMapper.writeValueAsString(executionStatus)
        val json = objectMapper.readTree(jsonString)
        val jsonField = json.get("executionState")
        assertTrue(jsonField.isTextual)
        assertEquals(ExecutionState.NO_STATE.value, json.get("executionState").asText())
    }

    @Test
    fun testCustomStateSerialization() {
        val objectMapper = ObjectMapper()
        val executionStatus = ExecutionStatus()
        executionStatus.executionState = ExecutionState.PENDING
        val jsonString = objectMapper.writeValueAsString(executionStatus)
        val json = objectMapper.readTree(jsonString)
        val jsonField = json.get("executionState")
        assertTrue(jsonField.isTextual)
        assertEquals(ExecutionState.PENDING.value, json.get("executionState").asText())
    }

    @Test
    fun testStateDeserialization() {
        val objectMapper = ObjectMapper()
        val json = objectMapper.createObjectNode()
        json.put("executionState", ExecutionState.RUNNING.value)
        json.put("executionDuration", "")
        val jsonString = objectMapper.writeValueAsString(json)
        val executionStatus = objectMapper.readValue(jsonString, ExecutionStatus::class.java)
        val executionState =  executionStatus.executionState
        assertNotNull(executionState)
        assertEquals(ExecutionState.RUNNING, executionState)
    }

    @Test
    fun testInvalidStateDeserialization() {
        val objectMapper = ObjectMapper()
        val json = objectMapper.createObjectNode()
        json.put("executionState", "invalid-state")
        json.put("executionDuration", "")
        val jsonString = objectMapper.writeValueAsString(json)
        assertThrows<InvalidFormatException> {
            objectMapper.readValue(jsonString, ExecutionStatus::class.java)
        }
    }

    @Test
    fun `test duration for no start and completion time`() {
        val executionStatus = ExecutionStatus()
        assertNull(executionStatus.startTime)
        assertNull(executionStatus.completionTime)
        assertNull(executionStatus.executionDuration)
    }

    @Test
    fun `test duration for no start but completion time`() {
        val executionStatus = ExecutionStatus()
        executionStatus.completionTime = MicroTime(Instant.parse("2022-01-02T18:59:20.492103Z").toString())
        assertNull(executionStatus.startTime)
        assertNull(executionStatus.executionDuration)
    }

    @Test
    fun `test duration for non completed execution`() {
        val startInstant = Instant.parse("2022-01-02T18:59:20.492103Z")
        val sinceStart = Duration.ofMinutes(5)
        val executionStatus = ExecutionStatus(clock = Clock.fixed(startInstant.plus(sinceStart), ZoneId.systemDefault()))
        executionStatus.startTime = MicroTime(startInstant.toString())
        assertNotNull(executionStatus.executionDuration)
        assertEquals(K8sDuration(sinceStart), executionStatus.executionDuration)
    }

    @Test
    fun `test duration for completed execution`() {
        val startInstant = Instant.parse("2022-01-02T18:59:20.492103Z")
        val sinceStart = Duration.ofMinutes(5)
        val executionStatus = ExecutionStatus()
        executionStatus.startTime = MicroTime(startInstant.toString())
        executionStatus.completionTime = MicroTime(startInstant.plus(sinceStart).toString())
        assertNotNull(executionStatus.executionDuration)
        assertEquals(K8sDuration(sinceStart), executionStatus.executionDuration)
    }

    @Test
    fun testDurationSerialization() {
        val objectMapper = ObjectMapper()
        val executionStatus = ExecutionStatus()
        val startInstant = Instant.parse("2022-01-02T18:59:20.492103Z")
        executionStatus.startTime = MicroTime(startInstant.toString())
        executionStatus.completionTime = MicroTime(startInstant.plus(Duration.ofMinutes(15)).toString())
        val jsonString = objectMapper.writeValueAsString(executionStatus)
        val json = objectMapper.readTree(jsonString)
        val jsonField = json.get("executionDuration")
        assertTrue(jsonField.isTextual)
        assertEquals("15m", jsonField.asText())
    }

    @Test
    fun testNotStartedDurationSerialization() {
        val objectMapper = ObjectMapper()
        val executionStatus = ExecutionStatus()
        val jsonString = objectMapper.writeValueAsString(executionStatus)
        val json = objectMapper.readTree(jsonString)

        assertTrue(json.get("startTime").isNull)
        assertTrue(json.get("completionTime").isNull)
        assertTrue(json.get("executionDuration").isNull)
    }

    @Test
    fun testWrongDurationDeserialization() {
        val startTime = "2022-01-02T18:59:20.492103Z"
        val completionTime = "2022-01-02T19:14:20.492103Z"
        val objectMapper = ObjectMapper()
        val json = objectMapper.createObjectNode()
        json.put("executionState", ExecutionState.RUNNING.value)
        json.put("executionDuration", "20m")
        json.put("startTime", startTime)
        json.put("completionTime", completionTime)
        val jsonString = objectMapper.writeValueAsString(json)
        val executionStatus = objectMapper.readValue(jsonString, ExecutionStatus::class.java)
        assertNotNull(executionStatus.executionDuration)
        assertEquals(Duration.ofMinutes(15), executionStatus.executionDuration?.duration)
    }
}