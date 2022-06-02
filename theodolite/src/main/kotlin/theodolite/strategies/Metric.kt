package theodolite.strategies

enum class Metric(val value: String) {
    DEMAND("demand"),
    CAPACITY("capacity");

    companion object {
        fun from(metric: String): Metric =
                values().find { it.value == metric } ?: throw IllegalArgumentException("Requested Metric does not exist")
    }
}