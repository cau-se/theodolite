package rocks.theodolite.kubernetes.slo

enum class SloTypes(val value: String) {
    GENERIC("generic"),
    LAG_TREND("lag trend"),
    @Deprecated("Use LAG_TREND with relative threshold instead.") LAG_TREND_RATIO("lag trend ratio"),
    @Deprecated("Use GENERIC instead.") DROPPED_RECORDS("dropped records"),
    @Deprecated("Use GENERIC with relative threshold instead.") DROPPED_RECORDS_RATIO("dropped records ratio");

    companion object {
        fun from(type: String): SloTypes =
            values().find { it.value == type } ?: throw IllegalArgumentException("Requested SLO does not exist")
    }
}