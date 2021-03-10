package theodolite.evaluation

import khttp.get

class SLOCheckerImpl(private val prometheusURL: String): SLOChecker {

    override fun evaluate(start: Long, end: Long): Boolean {
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL)
        val totalLag = metricFetcher.fetchMetric(start, end, "sum by(group)(kafka_consumergroup_group_lag > 0)")
        val parameter = mapOf("total_lag" to totalLag.toString())
        val response = get("http://127.0.0.1:8000/evaluate-slope", params = parameter)
        return response.jsonObject["suitable"] == "true"
    }
}