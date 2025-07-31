package rocks.theodolite.kubernetes.slo

interface MetricQueryResponseFactory<T : MetricQueryResponse> {
    fun fromString(json: String): T
}
