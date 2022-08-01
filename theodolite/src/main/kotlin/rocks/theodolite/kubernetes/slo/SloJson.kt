package rocks.theodolite.kubernetes.slo

import com.fasterxml.jackson.databind.ObjectMapper
import rocks.theodolite.kubernetes.util.PromResult

class SloJson constructor(
    val results: List<List<PromResult>>,
    var metadata: Map<String, Any>
) {

    fun toJson(): String {
        return ObjectMapper().writeValueAsString(
            mapOf(
                "results" to this.results,
                "metadata" to this.metadata
            )
        )
    }
}