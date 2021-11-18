package theodolite.evaluation

import theodolite.util.InvalidPatcherConfigurationException
import javax.enterprise.context.ApplicationScoped

private const val CONSUMER_LAG_QUERY = "sum by(group)(kafka_consumergroup_group_lag >= 0)"
private const val DROPPED_RECORDS_QUERY = "sum by(job) (kafka_streams_stream_task_metrics_dropped_records_total>=0)"

@ApplicationScoped
class SloConfigHandler() {
    companion object {
        fun getQueryString(sloType: String): String {
            return when (sloType.toLowerCase()) {
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_RATIO.value -> CONSUMER_LAG_QUERY
                SloTypes.DROPPED_RECORDS.value, SloTypes.DROPPED_RECORDS_RATIO.value -> DROPPED_RECORDS_QUERY
                else -> throw  InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type $sloType")
            }
        }
    }
}