package theodolite.benchmark

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize
enum class RolloutMode(@JsonValue val value: String) {
    NONE("no-waiting"),
    DEFAULT("default")
}