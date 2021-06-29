package theodolite.model.crd

enum class States(val value: String) {
    RUNNING("RUNNING"),
    PENDING("PENDING"),
    FAILURE("FAILURE"),
    FINISHED("FINISHED"),
    RESTART("RESTART"),
    INTERRUPTED("INTERRUPTED"),
    NO_STATE("NoState")
}