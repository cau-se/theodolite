package rocks.theodolite.kubernetes.slo

import com.google.gson.Gson

class SloJson constructor(
    val results: List<List<PromResult>>,
    var metadata: Map<String, Any>
) {

    fun toJson(): String {
        return Gson().toJson(
            mapOf(
                "results" to this.results,
                "metadata" to this.metadata
            )
        )
    }
}