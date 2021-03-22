package theodolite.evaluation

import theodolite.util.PrometheusResponse
import java.time.Instant

interface SloChecker {
    fun evaluate(start: Instant, end: Instant, fetchedData: PrometheusResponse): Boolean
}
