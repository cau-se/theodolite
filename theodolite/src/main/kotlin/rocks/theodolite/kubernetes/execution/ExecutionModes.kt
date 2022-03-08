package rocks.theodolite.kubernetes.execution

enum class ExecutionModes(val value: String) {
    OPERATOR("operator"),
    STANDALONE("standalone")
}