package rocks.theodolite.kubernetes.slo

import io.smallrye.config.ConfigMapping
import rocks.theodolite.kubernetes.model.KubernetesBenchmark.Slo
import rocks.theodolite.kubernetes.patcher.InvalidPatcherConfigurationException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

private const val DEFAULT_CONSUMER_LAG_METRIC_BASE = "kafka_consumergroup_lag"
private const val DEFAULT_CONSUMER_LAG_QUERY = "sum by(consumergroup) (kafka_consumergroup_lag >= 0)"

@ApplicationScoped
class SloConfigHandler @Inject constructor(
    val config: SloConfig
){
    companion object {
        fun getQueryString(slo: Slo): String {
            return when (slo.sloType.lowercase()) {
                SloTypes.GENERIC.value -> slo.properties["query"] ?: throw IllegalArgumentException("query expected")
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_RATIO.value -> slo.properties["query"] ?:
                    (slo.properties["consumerGroup"]?.let { "{consumergroup='$it'}" } ?: "").let {
                        "sum by(consumergroup) ($DEFAULT_CONSUMER_LAG_METRIC_BASE$it >= 0)"
                    }
                SloTypes.LAG_TREND.value, SloTypes.LAG_TREND_RATIO.value -> slo.properties["query"] ?: DEFAULT_CONSUMER_LAG_QUERY // TODO reachable?
                else -> throw InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type ${slo.sloType}")
            }
        }
    }
}

@ConfigMapping(prefix = "dql")
interface SloConfig {
    fun clientid(): String
    fun clientsecret(): String
    fun scope(): String
    fun resource(): String
    fun authurl(): String
    fun queryurl(): String
}
