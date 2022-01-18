package theodolite.evaluation

enum class SloTypes(val value: String) {
    GENERIC("generic"),
    LAG_TREND("lag trend"),
    LAG_TREND_RATIO("lag trend ratio"),
    DROPPED_RECORDS("dropped records"),
    DROPPED_RECORDS_RATIO("dropped records ratio");

    companion object {
        fun from(type: String): SloTypes =
            values().find { it.value == type } ?: throw IllegalArgumentException("Requested SLO does not exist")
    }
}