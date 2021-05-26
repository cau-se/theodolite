package theodolite.model.crd

enum class STATES(val value: String) {
    Running("RUNNING"),
    Pending("PENDING"),
    Failure("FAILURE"),
    Finished("FINISHED"),
    Restart("RESTART"),
    Interrupted("INTERRUPTED"),
    NoState("NoState")
}