package rocks.theodolite.kubernetes

enum class ExecutionModes(val value: String) {
    OPERATOR("operator"),
    STANDALONE("standalone")
}