package rocks.theodolite.kubernetes

enum class ExecutionModes(val value: String) {
    OPERATOR("operator"),
    @Deprecated("Standalone mode is deprecated and will be removed in future versions.")
    STANDALONE("standalone")
}