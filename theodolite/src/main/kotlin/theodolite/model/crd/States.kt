package theodolite.model.crd

enum class States(val value: String) {
    // Execution states
    RUNNING("RUNNING"),
    PENDING("PENDING"),
    FAILURE("FAILURE"),
    FINISHED("FINISHED"),
    RESTART("RESTART"),
    INTERRUPTED("INTERRUPTED"),
    NO_STATE("NoState"),

    // Benchmark states
    AVAILABLE("AVAILABLE"),
    NOT_AVAILABLE("NOT_AVAILABLE")

}