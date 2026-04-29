package rocks.theodolite.kubernetes.slo

interface MetricQueryResponse {
    /**
     * Return the data as [List] of [List]s of [String]s.
     * Format: `[[ group, timestamp, value ], ...]`
     */
    fun getResultAsList(onlyFirst: Boolean = true): List<List<String>>

    fun isNullOrEmpty(): Boolean

    /**
     * Return the data formatted for the external SLO checker payload.
     * When [onlyFirst] is true, only the first time series per response is included.
     */
    fun getDataForSLOChecker(onlyFirst: Boolean = true): List<SloJson.MetricResult>
}
