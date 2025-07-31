package rocks.theodolite.kubernetes.slo

object MetricRequestExecutorFactory {

    private val executors: MutableMap<MetricFetcher.Kind, MetricRequestExecutor> = mutableMapOf()

    fun get(kind: MetricFetcher.Kind): MetricRequestExecutor =
        executors.getOrPut(kind) {
            when (kind) {
                MetricFetcher.Kind.PROMETHEUS -> PrometheusRequestExecutor()
                MetricFetcher.Kind.DYNATRACE -> DQLRequestExecutor()
            }
        }
}
