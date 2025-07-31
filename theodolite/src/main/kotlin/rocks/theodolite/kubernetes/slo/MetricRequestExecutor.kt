package rocks.theodolite.kubernetes.slo

import java.net.URI
import java.time.Duration
import java.time.Instant

interface MetricRequestExecutor {
    fun executeRequest(
        uri: URI,
        query: String,
        offsetStart: Instant,
        offsetEnd: Instant,
        stepSize: Duration,
        timeout: Duration
    ): MetricQueryResponse
}
