package theodolite.evaluation

import theodolite.util.InvalidPatcherConfigurationException
import theodolite.util.TheodoliteConfig
import javax.inject.Inject

class SloConfigHandler {

    companion object {
        @Inject
        lateinit var config: TheodoliteConfig

        fun getQueryString(sloType: String): String {
            return when (sloType){
                "Lag Trend" -> config.PROM_RECORD_LAG_QUERY
                "Dropped Records" -> config.PROM_DROPPED_RECORDS_QUERY
                else -> throw  InvalidPatcherConfigurationException("Could not find Prometheus query string for slo type $sloType")
            }
        }
    }
}