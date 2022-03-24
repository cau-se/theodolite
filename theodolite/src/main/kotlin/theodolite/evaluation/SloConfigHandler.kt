package theodolite.evaluation

import theodolite.benchmark.BenchmarkExecution
import theodolite.util.InvalidPatcherConfigurationException
import javax.enterprise.context.ApplicationScoped

private const val DEFAULT_CONSUMER_LAG_QUERY = "sum by(consumergroup) (kafka_consumergroup_lag >= 0)"
private const val DEFAULT_DROPPED_RECORDS_QUERY = "sum by(job) (kafka_streams_stream_task_metrics_dropped_records_total>=0)"

@ApplicationScoped
class SloConfigHandler {
    companion object {
        fun getQueryString(slo: BenchmarkExecution.Slo): String {
            return when (slo.sloType.lowercase()) {
                SloTypes.GENERIC.value -> slo.properties["promQLQuery"] ?: throw IllegalArgumentException("promQLQuery expected")
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_RATIO.value -> slo.properties["promQLQuery"] ?: DEFAULT_CONSUMER_LAG_QUERY
                SloTypes.DROPPED_RECORDS.value, SloTypes.DROPPED_RECORDS_RATIO.value -> slo.properties["promQLQuery"] ?: DEFAULT_DROPPED_RECORDS_QUERY
                else -> throw  InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type $slo.sloType")
            }
        }
    }
}