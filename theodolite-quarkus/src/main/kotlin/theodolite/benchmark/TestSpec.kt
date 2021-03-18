package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
data class TestSpec(var message: String = "MEGAAIDS") {
}