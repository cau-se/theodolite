package rocks.theodolite.kubernetes.slo

import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Sli

/**
 * Creates the appropriate [MetricFetcher] implementation based on [Sli.provider].
 */
object MetricFetcherFactory {
    fun create(sli: Sli): MetricFetcher = when (sli.provider.lowercase()) {
        "prometheus" -> PrometheusMetricFetcher(sli)
        "dynatrace" -> DynatraceMetricFetcher(sli)
        else -> throw IllegalArgumentException(
            "Unknown SLI provider '${sli.provider}' for SLI '${sli.name}'. Supported: prometheus, dynatrace."
        )
    }
}
