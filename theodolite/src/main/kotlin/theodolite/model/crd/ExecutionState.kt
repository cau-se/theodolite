package theodolite.model.crd

import com.fasterxml.jackson.annotation.JsonValue

enum class ExecutionState(@JsonValue val value: String) {
    RUNNING("Running"),
    PENDING("Pending"),
    FAILURE("Failure"),
    FINISHED("Finished"),
    RESTART("Restart"),
    INTERRUPTED("Interrupted"),
    NO_STATE("NoState"),
}