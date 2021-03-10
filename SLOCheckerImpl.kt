package theodolite.evaluation

import khttp.get

class SLOCheckerImpl(private val prometheusURL: String): SLOChecker {

    override fun evaluate(start: Long, end: Long): Boolean {
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL)
        val totalLag = metricFetcher.fetchMetric(start, end, "sum by(group)(kafka_consumergroup_group_lag > 0)")
        print(totalLag)
        // val parameter = mapOf("" to "")
        // val response = get("", params = parameter)
        // TODO("call python evaluator")

        return false
    }
}