package theodolite.evaluation

import java.time.Instant

interface SLOChecker {
    fun evaluate(start: Instant, end: Instant): Boolean
}
