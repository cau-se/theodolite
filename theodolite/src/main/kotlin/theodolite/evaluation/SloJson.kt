package theodolite.evaluation

import com.google.gson.Gson
import theodolite.util.PromResult

class SloJson private constructor(
    val results: List<List<PromResult>?>? = null,
    var metadata: MutableMap<String, Any>? = null
) {

    data class Builder(
        var results:List<List<PromResult>?>? = null,
        var metadata: MutableMap<String, Any>? = null
    ) {

        /**
         *  Set the results
         *
         * @param results list of prometheus results
         */
        fun results(results: List<List<PromResult>?>) = apply { this.results = results }

        /**
         * Add metadata as key value pairs
         *
         * @param key key of the metadata to be added
         * @param value value of the metadata to be added
         */
        fun addMetadata(key: String, value: Any) = apply {
            if (this.metadata.isNullOrEmpty()) {
                this.metadata = mutableMapOf(key to value)
            } else {
                this.metadata!![key] = value
            }
        }

        /**
         * Add metadata as map of key value pairs.
         *
         * @param metadata map of key-value pairs to be added to be added
         */
        fun addMetadata(metadata: Map<String, Any>) = apply {
            for (entry in metadata) {
                this.addMetadata(entry.key, entry.value)
            }
        }

        fun build() = SloJson(
            results = results,
            metadata = metadata
        )
    }

   fun  toJson(): String {
       return Gson().toJson(mapOf(
           "results" to this.results,
           "metadata" to this.metadata
       ))
    }
}