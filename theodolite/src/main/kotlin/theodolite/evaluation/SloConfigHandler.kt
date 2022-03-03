package theodolite.evaluation

import theodolite.benchmark.BenchmarkExecution
import theodolite.util.InvalidPatcherConfigurationException
import javax.enterprise.context.ApplicationScoped

private const val CONSUMER_LAG_METRIC = "kafka_consumergroup_lag"
private const val DROPPED_RECORDS_QUERY = "sum by(job) (kafka_streams_stream_task_metrics_dropped_records_total>=0)"

@ApplicationScoped
class SloConfigHandler {
    companion object {
        fun getQueryString(slo: BenchmarkExecution.Slo): String {
            return when (slo.sloType.toLowerCase()) {
                SloTypes.GENERIC.value -> slo.properties["promQLQuery"] ?: throw IllegalArgumentException("promQLQuery expected")
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_RATIO.value -> {
                    var projection: String;
                    if (slo.properties.containsKey("consumerGroup")) {
                        projection = "consumergroup='" + slo.properties["consumerGroup"] + "'";
                    } else projection = "";
                    return "sum by(consumergroup) ($CONSUMER_LAG_METRIC{$projection} >= 0)";
                }
                SloTypes.DROPPED_RECORDS.value, SloTypes.DROPPED_RECORDS_RATIO.value -> DROPPED_RECORDS_QUERY
                else -> throw  InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type $slo.sloType")
            }
        }
    }
}