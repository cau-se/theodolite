package rocks.theodolite.kubernetes.slo


interface MetricQueryResponse {
    /**
     * Return the data of the Response as [List] of [List]s of [String]s
     * The format of the returned list is: `[[ group, timestamp, value ], [ group, timestamp, value ], ... ]`
     */
    fun getResultAsList(onlyFirst: Boolean = true): List<List<String>>
    fun isNullOrEmpty(): Boolean
    fun getDataForSLOChecker(): List<SloJson.MetricResult>
}
