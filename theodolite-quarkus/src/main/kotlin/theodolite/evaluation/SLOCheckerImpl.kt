package theodolite.evaluation

import khttp.post
import org.json.JSONObject

class SLOCheckerImpl(private val prometheusURL: String) : SLOChecker {

    override fun evaluate(start: Long, end: Long): Boolean {
        val metricFetcher = MetricFetcher(prometheusURL = prometheusURL)
        val totalLag = metricFetcher.fetchMetric(start, end, "sum by(group)(kafka_consumergroup_group_lag > 0)")
        val parameter = mapOf("total_lag" to totalLag)

        //val response = get("http://127.0.0.1:8000/evaluate-slope", params = parameter)
        val post = post("http://127.0.0.1:8000/evaluate-slope", data = JSONObject(parameter))
        //println(JSONObject(parameter))
        //println(post.text.toBoolean())
        return post.text.toBoolean()//response.jsonObject["suitable"] == "true"
    }
}
