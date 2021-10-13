package theodolite.util

import io.quarkus.arc.config.ConfigProperties
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Singleton


@ApplicationScoped
class TheodoliteConfig{

    @ConfigProperty(name = "prom.dropped.records.query")
    lateinit var PROM_DROPPED_RECORDS_QUERY: String

    @ConfigProperty(name = "prom.record.lag.query")
    lateinit var PROM_RECORD_LAG_QUERY: String
}