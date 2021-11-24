package theodolite.util

import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.ExecutionStates

class ExecutionStateComparator(private val preferredState: ExecutionStates): Comparator<ExecutionCRD> {

    /**
     * Simple comparator which can be used to order a list of [ExecutionCRD] such that executions with
     * status [ExecutionStates.RESTART] are before all other executions.
     */
    override fun compare(p0: ExecutionCRD, p1: ExecutionCRD): Int {
       return when {
            (p0 == null && p1 == null) -> 0
            (p0.status.executionState == preferredState.value) -> -1
            else -> 1
        }
    }
}