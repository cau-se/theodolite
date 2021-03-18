package theodolite.evaluation

import java.time.Instant

interface SloChecker {
    fun evaluate(start: Instant, end: Instant): Boolean
}
