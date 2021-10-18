package theodolite.evaluation

import theodolite.util.InvalidPatcherConfigurationException
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SloConfigHandler() {
    companion object {
        fun getQueryString(sloType: String): String {
            return when (sloType.toLowerCase()) {
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_PERCENTAGE.value -> "sum by(group)(kafka_consumergroup_group_lag >= 0)"
                SloTypes.DROPPED_RECORDS.value, SloTypes.DROPPED_RECORDS_PERCENTAGE.value -> "sum by(job) (kafka_streams_stream_task_metrics_dropped_records_total>=0)"
                else -> throw  InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type $sloType")
            }
        }
    }
}