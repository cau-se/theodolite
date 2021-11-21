package theodolite.execution

enum class ExecutionModes(val value: String) {
    OPERATOR("operator"),
    YAML_EXECUTOR("yaml-executor"),
    STANDALONE("standalone")
}