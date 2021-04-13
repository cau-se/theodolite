package theodolite.evaluation

import theodolite.util.PrometheusResponse
import java.time.Instant

interface SloChecker {
    fun evaluate(fetchedData: List<PrometheusResponse>): Boolean
}
