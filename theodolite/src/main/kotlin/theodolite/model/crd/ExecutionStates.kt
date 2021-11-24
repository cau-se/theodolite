package theodolite.model.crd

enum class ExecutionStates(val value: String) {
    // Execution states
    RUNNING("Running"),
    PENDING("Pending"),
    FAILURE("Failure"),
    FINISHED("Finished"),
    RESTART("Restart"),
    INTERRUPTED("Interrupted"),
    NO_STATE("NoState"),
}