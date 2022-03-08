package rocks.theodolite.kubernetes.util

import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState

class ExecutionStateComparator(private val preferredState: ExecutionState): Comparator<ExecutionCRD> {

    /**
     * Simple comparator which can be used to order a list of [ExecutionCRD]s such that executions with
     * status [ExecutionState.RESTART] are before all other executions.
     */
    override fun compare(p0: ExecutionCRD, p1: ExecutionCRD): Int {
       return when {
            (p0.status.executionState == preferredState) -> -1
            else -> 1
        }
    }
}