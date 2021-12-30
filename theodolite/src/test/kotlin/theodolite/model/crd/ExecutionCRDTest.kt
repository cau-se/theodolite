package theodolite.model.crd

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


internal class ExecutionCRDTest {

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
}